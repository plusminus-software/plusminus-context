package software.plusminus.context;

import java.util.Map;
import java.util.Optional;

public interface WritableContext<T> extends Context<T> {

    @Override
    default T provide() {
        throw new IllegalStateException("Missed value int the context for " + this);
    }

    default void set(T value) {
        Map<Context<?>, Object> values = VALUES.get();
        if (values == null) {
            throw new IllegalStateException("Context is not initialized");
        }
        Object previous = values.putIfAbsent(this, value);
        if (previous != null) {
            throw new IllegalStateException("Cannot update context to " + value
                    + " as the context already contains " + previous);
        }
    }

    default Optional<T> getIfPresent() {
        Map<Context<?>, Object> values = VALUES.get();
        if (values == null) {
            throw new IllegalStateException("Context is not initialized");
        }
        return Optional.ofNullable((T) values.get(this));
    }

    static <T> WritableContext<T> of() {
        return new SimpleWritableContext<>();
    }

    static <T> WritableContext<T> of(T value) {
        WritableContext<T> context = new SimpleWritableContext<>();
        context.set(value);
        return context;
    }
}
