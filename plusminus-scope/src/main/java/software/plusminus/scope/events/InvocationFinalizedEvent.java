package software.plusminus.scope.events;

public class InvocationFinalizedEvent<T> extends AbstractInvocationEvent<T> {
    public InvocationFinalizedEvent(T target) {
        super(target);
    }
}
