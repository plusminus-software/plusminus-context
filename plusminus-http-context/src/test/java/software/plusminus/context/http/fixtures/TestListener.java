package software.plusminus.context.http.fixtures;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import software.plusminus.scope.events.ScopeCompletedEvent;
import software.plusminus.scope.events.ScopeFailedEvent;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;

import java.util.ArrayList;
import java.util.List;

@Component
public class TestListener {

    public List<String> calls = new ArrayList<>();

    @EventListener
    void started(ScopeStartedEvent event) {
        calls.add("started");
    }

    @EventListener
    void completed(ScopeCompletedEvent event) {
        calls.add("completed");
    }

    @EventListener
    void failed(ScopeFailedEvent event) {
        calls.add("failed");
    }

    @EventListener
    void finalized(ScopeFinalizedEvent event) {
        calls.add("finalized");
    }
}
