package software.plusminus.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;
import software.plusminus.scope.fixtures.FirstAroundScope;
import software.plusminus.scope.fixtures.NotSupportedAroundScope;
import software.plusminus.scope.fixtures.SecondAroundScope;
import software.plusminus.scope.fixtures.TestListener;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static software.plusminus.check.Checks.check;

@SpringBootTest
class ScopeRunnerTest {

    @Autowired
    private TestListener testListener;
    @SpyBean
    private FirstAroundScope firstAroundScope;
    @SpyBean
    private SecondAroundScope secondAroundScope;
    @SpyBean
    private NotSupportedAroundScope notSupportedAroundScope;
    @Autowired
    private ScopeRunner scopeRunner;

    @AfterEach
    void afterEach() {
        testListener.calls().clear();
    }

    @Test
    void success() {
        scopeRunner.run(this, () -> { });
        check(testListener.calls()).is("started", "completed", "finalized");
    }

    @Test
    void exception() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> scopeRunner.run(this, () -> {
            throw new RuntimeException("Test exception");
        }));
        check(exception.getMessage()).is("Test exception");
        check(testListener.calls()).is("started", "failed", "finalized");
    }

    @Test
    void error() {
        AssertionError error = assertThrows(AssertionError.class, () -> scopeRunner.run(this, () -> {
            throw new AssertionError("Test error");
        }));
        check(error.getMessage()).is("Test error");
        check(testListener.calls()).is("started", "failed", "finalized");
    }

    @Test
    void deferredScopeIsNotFinishedUntilCompleted() {
        ScopeCompletion completion = scopeRunner.runDeferred(this, () -> { });
        check(testListener.calls()).is("started");

        completion.complete();
        completion.complete();
        check(testListener.calls()).is("started", "completed", "finalized");
    }

    @Test
    void deferredScopeCanFailLater() {
        ScopeCompletion completion = scopeRunner.runDeferred(this, () -> { });

        completion.fail(new IllegalStateException("Async failure"));
        completion.complete();
        check(testListener.calls()).is("started", "failed", "finalized");
    }

    @Test
    void deferredScopeFailsImmediatelyWhenBodyFails() {
        assertThrows(RuntimeException.class, () -> scopeRunner.runDeferred(this, () -> {
            throw new RuntimeException("Test exception");
        }));
        check(testListener.calls()).is("started", "failed", "finalized");
    }

    @Test
    void around() {
        Runnable runnable = mock(Runnable.class);
        ThrowingRunnable throwingRunnable = ThrowingRunnable.of(runnable);
        scopeRunner.run(this, throwingRunnable);

        InOrder inOrder = Mockito.inOrder(firstAroundScope, secondAroundScope);
        inOrder.verify(firstAroundScope).supports(this);
        inOrder.verify(secondAroundScope).supports(this);
        inOrder.verify(firstAroundScope).around(any());
        inOrder.verify(secondAroundScope).around(any());
        verify(notSupportedAroundScope, never()).around(any());
        verify(runnable).run();
    }

    @Test
    void aroundWithoutEvents() {
        Runnable runnable = mock(Runnable.class);

        scopeRunner.around(this, ThrowingRunnable.of(runnable));

        InOrder inOrder = Mockito.inOrder(firstAroundScope, secondAroundScope);
        inOrder.verify(firstAroundScope).around(any());
        inOrder.verify(secondAroundScope).around(any());
        verify(notSupportedAroundScope, never()).around(any());
        verify(runnable).run();
        check(testListener.calls()).isEmpty();
    }

    @Test
    void failedStartListenerIsFinalized() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        doThrow(new IllegalStateException("start")).when(publisher).publishEvent(any(ScopeStartedEvent.class));
        doThrow(new IllegalStateException("finalize")).when(publisher).publishEvent(any(ScopeFinalizedEvent.class));
        ScopeRunner runner = new ScopeRunner(publisher, Collections.emptyList());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> runner.run(this, () -> { }));

        check(exception.getMessage()).is("start");
        check(exception.getSuppressed()[0].getMessage()).is("finalize");
        verify(publisher).publishEvent(any(ScopeFinalizedEvent.class));
    }
}
