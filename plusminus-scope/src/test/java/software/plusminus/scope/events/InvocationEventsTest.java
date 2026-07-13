package software.plusminus.scope.events;

import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

import static org.assertj.core.api.Assertions.assertThat;

class InvocationEventsTest {

    @Test
    void startedEvent() {
        InvocationStartedEvent<String> event = new InvocationStartedEvent<>("target");
        event.setIntercepted(true);
        assertThat(event.getTarget()).isEqualTo("target");
        assertThat(event.isIntercepted()).isTrue();
        assertThat(event.getResolvableType())
                .isEqualTo(ResolvableType.forClassWithGenerics(InvocationStartedEvent.class, String.class));
    }

    @Test
    void completedEventWithResult() {
        InvocationCompletedEvent<String, Integer> event = new InvocationCompletedEvent<>("target", 42);
        assertThat(event.getTarget()).isEqualTo("target");
        assertThat(event.getResult()).isEqualTo(42);
        assertThat(event.getResolvableType())
                .isEqualTo(ResolvableType.forClassWithGenerics(
                        InvocationCompletedEvent.class, String.class, Integer.class));
    }

    @Test
    void completedEventWithoutResult() {
        InvocationCompletedEvent<String, Integer> event = new InvocationCompletedEvent<>("target", null);
        assertThat(event.getResult()).isNull();
        assertThat(event.getResolvableType())
                .isEqualTo(ResolvableType.forClassWithGenerics(
                        InvocationCompletedEvent.class, ResolvableType.forInstance("target"), null));
    }

    @Test
    void failedEvent() {
        IllegalStateException exception = new IllegalStateException();
        InvocationFailedEvent<String, IllegalStateException> event =
                new InvocationFailedEvent<>("target", exception);
        assertThat(event.getTarget()).isEqualTo("target");
        assertThat(event.getException()).isSameAs(exception);
        assertThat(event.getResolvableType())
                .isEqualTo(ResolvableType.forClassWithGenerics(
                        InvocationFailedEvent.class, String.class, IllegalStateException.class));
    }

    @Test
    void finalizedEvent() {
        InvocationFinalizedEvent<String> event = new InvocationFinalizedEvent<>("target");
        assertThat(event.getTarget()).isEqualTo("target");
    }
}
