package software.plusminus.scope.events;

import lombok.Getter;
import lombok.Setter;

public class InvocationStartedEvent<T> extends AbstractInvocationEvent<T> {

    @Getter
    @Setter
    private boolean intercepted;

    public InvocationStartedEvent(T target) {
        super(target);
    }
}
