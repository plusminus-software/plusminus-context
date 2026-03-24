package software.plusminus.scope.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

@AllArgsConstructor
public class AbstractInvocationEvent<T> implements ResolvableTypeProvider {

    @Getter
    private T target;

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(target));
    }
}
