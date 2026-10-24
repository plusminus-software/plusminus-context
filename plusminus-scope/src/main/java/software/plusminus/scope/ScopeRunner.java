package software.plusminus.scope;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import software.plusminus.scope.events.ScopeCompletedEvent;
import software.plusminus.scope.events.ScopeFailedEvent;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@AllArgsConstructor
public class ScopeRunner {

    private ApplicationEventPublisher eventPublisher;
    private List<AroundScope> arounds;

    @SafeVarargs
    public final void run(Object source, ThrowingRunnable scope,
                          Class<? extends Exception>... exceptionsToExtract) {
        runDeferred(source, scope, exceptionsToExtract).complete();
    }

    /**
     * Runs the scope like {@link #run}, but does not finish it: the caller calls
     * {@link ScopeCompletion#complete()} or {@link ScopeCompletion#fail(Throwable)} when the work
     * really ends, for example when an async request completes. If the scope itself fails, the failed
     * and finalized events are published and the failure is rethrown.
     */
    @SafeVarargs
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public final ScopeCompletion runDeferred(Object source, ThrowingRunnable scope,
                                             Class<? extends Exception>... exceptionsToExtract) {
        Completion completion = new Completion(exceptionsToExtract);
        try {
            eventPublisher.publishEvent(new ScopeStartedEvent());
            AroundScope.around(arounds, source, scope.asRunnable()).run();
        } catch (Throwable t) {
            completion.fail(t);
            throw t;
        }
        return completion;
    }

    private final class Completion implements ScopeCompletion {

        private final AtomicBoolean finished = new AtomicBoolean();
        private final Class<? extends Exception>[] exceptionsToExtract;

        private Completion(Class<? extends Exception>[] exceptionsToExtract) {
            this.exceptionsToExtract = exceptionsToExtract;
        }

        @Override
        public void complete() {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            try {
                eventPublisher.publishEvent(new ScopeCompletedEvent());
            } finally {
                eventPublisher.publishEvent(new ScopeFinalizedEvent());
            }
        }

        @Override
        public void fail(Throwable failure) {
            if (!finished.compareAndSet(false, true)) {
                return;
            }
            Exception exception = failure instanceof Exception
                    ? ExceptionExtractor.extract((Exception) failure, exceptionsToExtract)
                    : new RuntimeException(failure);
            publishAfterFailure(new ScopeFailedEvent<>(exception), failure);
            publishAfterFailure(new ScopeFinalizedEvent(), failure);
        }

        private void publishAfterFailure(Object event, Throwable failure) {
            try {
                eventPublisher.publishEvent(event);
            } catch (RuntimeException e) {
                failure.addSuppressed(e);
            }
        }
    }
}
