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

    public void run(Object source, ThrowingRunnable scope) {
        run(source, scope.asRunnable());
    }

    private void run(Object source, Runnable scope) {
        eventPublisher.publishEvent(new ScopeStartedEvent());
        try {
            AroundScope.around(arounds, source, scope).run();
            eventPublisher.publishEvent(new ScopeCompletedEvent());
        } catch (Exception e) {
            eventPublisher.publishEvent(new ScopeFailedEvent(e));
            throw e;
        } finally {
            eventPublisher.publishEvent(new ScopeFinalizedEvent());
        }
    }
}
