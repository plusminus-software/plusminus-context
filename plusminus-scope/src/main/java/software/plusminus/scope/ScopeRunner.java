package software.plusminus.scope;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import software.plusminus.scope.events.ScopeCompletedEvent;
import software.plusminus.scope.events.ScopeFailedEvent;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;

import java.util.List;

@Component
@AllArgsConstructor
public class ScopeRunner {

    private ApplicationEventPublisher eventPublisher;
    private List<AroundScope> arounds;

    @SafeVarargs
    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    public final void run(Object source, ThrowingRunnable scope,
                          Class<? extends Exception>... exceptionsToExtract) {
        try {
            eventPublisher.publishEvent(new ScopeStartedEvent());
            AroundScope.around(arounds, source, scope.asRunnable()).run();
        } catch (Throwable t) {
            Exception failure = t instanceof Exception
                    ? ExceptionExtractor.extract((Exception) t, exceptionsToExtract)
                    : new RuntimeException(t);
            publishAfterFailure(new ScopeFailedEvent<>(failure), t);
            publishAfterFailure(new ScopeFinalizedEvent(), t);
            throw t;
        }
        try {
            eventPublisher.publishEvent(new ScopeCompletedEvent());
        } finally {
            eventPublisher.publishEvent(new ScopeFinalizedEvent());
        }
    }

    private void publishAfterFailure(Object event, Throwable failure) {
        try {
            eventPublisher.publishEvent(event);
        } catch (RuntimeException e) {
            failure.addSuppressed(e);
        }
    }
}
