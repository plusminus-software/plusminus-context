package software.plusminus.scope;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import software.plusminus.scope.fixtures.FirstAroundScope;
import software.plusminus.scope.fixtures.SecondAroundScope;
import software.plusminus.scope.fixtures.TestListener;
import software.plusminus.scope.fixtures.NotSupportedAroundScope;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
        testListener.calls.clear();
    }

    @Test
    void success() {
        scopeRunner.run(this, () -> { });
        check(testListener.calls).is("started", "completed", "finalized");
    }

    @Test
    void exception() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> scopeRunner.run(this, () -> {
            throw new RuntimeException("Test exception");
        }));
        check(exception.getMessage()).is("Test exception");
        check(testListener.calls).is("started", "failed", "finalized");
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
}