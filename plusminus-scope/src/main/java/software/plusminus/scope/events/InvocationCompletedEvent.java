package software.plusminus.scope.events;

import lombok.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

public class InvocationCompletedEvent<T, R> extends AbstractInvocationEvent<T>  {

    @Getter
    @Nullable
    private R result;

    public InvocationCompletedEvent(T target, R result) {
        super(target);
        this.result = result;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                getClass(),
                ResolvableType.forInstance(getTarget()),
                result == null ? null : ResolvableType.forInstance(result)
        );
    }
}
