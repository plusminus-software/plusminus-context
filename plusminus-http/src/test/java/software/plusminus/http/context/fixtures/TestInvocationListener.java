package software.plusminus.http.context.fixtures;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.scope.events.InvocationCompletedEvent;
import software.plusminus.scope.events.InvocationFailedEvent;
import software.plusminus.scope.events.InvocationFinalizedEvent;
import software.plusminus.scope.events.InvocationStartedEvent;

@Component
public class TestInvocationListener {

    @EventListener
    public void started(InvocationStartedEvent<HandlerMethod> event) {
        //Test method
    }

    @EventListener
    public void completed(InvocationCompletedEvent<HandlerMethod, ?> event) {
        //Test method
    }

    @EventListener
    public void failed(InvocationFailedEvent<HandlerMethod, ?> event) {
        //Test method
    }

    @EventListener
    public void failedWithSpecificException(InvocationFailedEvent<HandlerMethod, IllegalStateException> event) {
        //Test method
    }

    @EventListener
    public void failedWithUnknownException(InvocationFailedEvent<HandlerMethod, SecurityException> event) {
        //Test method
    }

    @EventListener
    public void finalized(InvocationFinalizedEvent<HandlerMethod> event) {
        //Test method
    }
}
