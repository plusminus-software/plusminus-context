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

        eventPublisher.publishEvent(new ScopeStartedEvent());
        try {
            AroundScope.around(arounds, source, scope.asRunnable()).run();
            eventPublisher.publishEvent(new ScopeCompletedEvent());
        } catch (Throwable t) {
            Exception failure = t instanceof Exception
                    ? ExceptionExtractor.extract((Exception) t, exceptionsToExtract)
                    : new RuntimeException(t);
            eventPublisher.publishEvent(new ScopeFailedEvent<>(failure));
            throw t;
        } finally {
            eventPublisher.publishEvent(new ScopeFinalizedEvent());
        }
    }
}
