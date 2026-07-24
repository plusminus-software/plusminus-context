package software.plusminus.context;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ContextPropagationUtil {

    public boolean isPropagatable(Context<?> context) {
        return !(context instanceof ConstantContext);
    }

    public <T> boolean replace(Context<T> context, Object value) {
        if (context instanceof ThreadLocalContext) {
            ((ThreadLocalContext<T>) context).replace((T) value);
            return true;
        }
        return false;
    }

    public void clear(Context<?> context) {
        if (context instanceof ClearableContext) {
            ((ClearableContext<?>) context).clear();
        }
    }
}
