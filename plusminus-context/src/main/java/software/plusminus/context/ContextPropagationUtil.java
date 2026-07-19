package software.plusminus.context;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ContextPropagationUtil {

    public boolean isPropagatable(Context<?> context) {
        return !(context instanceof ConstantContext);
    }

    public <T> void replace(Context<T> context, Object value) {
        if (context instanceof ThreadLocalContext) {
            ((ThreadLocalContext<T>) context).replace((T) value);
        }
    }

    public void clear(Context<?> context) {
        if (context instanceof ClearableContext) {
            ((ClearableContext<?>) context).clear();
        }
    }
}
