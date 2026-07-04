package software.plusminus.http.context.fixtures;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.plusminus.scope.events.ScopeCompletedEvent;
import software.plusminus.scope.events.ScopeFailedEvent;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;

@Component
public class TestScopeListener {

    @EventListener
    public void started(ScopeStartedEvent event) {
        //Test method
    }

    @EventListener
    public void completed(ScopeCompletedEvent event) {
        //Test method
    }

    @EventListener
    public void failed(ScopeFailedEvent<?> event) {
        //Test method
    }

    @EventListener
    public void unknownFailed(ScopeFailedEvent<? extends SecurityException> event) {
        //Test method
    }

    @EventListener
    public void finalized(ScopeFinalizedEvent event) {
        //Test method
    }
}
