package software.plusminus.scope.events;

import lombok.Getter;
import org.springframework.core.ResolvableType;

public class InvocationFailedEvent<T, E extends Exception> extends AbstractInvocationEvent<T> {

    @Getter
    private E exception;

    public InvocationFailedEvent(T target, E exception) {
        super(target);
        this.exception = exception;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                getClass(),
                ResolvableType.forInstance(getTarget()),
                ResolvableType.forInstance(exception)
        );
    }
}
