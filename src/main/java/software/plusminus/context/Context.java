package software.plusminus.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public interface Context<T> {

    ThreadLocal<Map<Context<?>, Object>> VALUES = new ThreadLocal<>();

    default T get() {
        Map<Context<?>, Object> values = VALUES.get();
        if (values == null) {
            throw new IllegalStateException("Context is not initialized");
        }
        return (T) values.computeIfAbsent(this, self -> provide());
    }

    default Optional<T> optional() {
        return Optional.ofNullable(get());
    }

    T provide();

    static void init() {
        VALUES.set(new ConcurrentHashMap<>());
    }

    static void clear() {
        VALUES.remove();
    }

    static <T> Context<T> of(Supplier<T> provider) {
        return new SimpleContext<>(provider);
    }
}
