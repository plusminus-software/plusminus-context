package software.plusminus.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

final class Scope implements AutoCloseable {

    private static final ThreadLocal<Scope> CURRENT = new ThreadLocal<>();

    private final Scope outer = CURRENT.get();
    private final Map<Context<?>, Object> bound = new HashMap<>();
    private final Map<Context<?>, Object> saved = new HashMap<>();

    private Scope() {
    }

    @SuppressWarnings("unchecked")
    private <T> T save(Context<? extends T> context, Supplier<? extends T> supplier) {
        Object value = saved.get(context);
        if (value == null) {
            value = supplier.get();
            if (value != null) {
                saved.put(context, value);
            }
        }
        return (T) value;
    }

    private Map<Context<?>, Object> copy(boolean includeSaves) {
        Map<Context<?>, Object> values = new HashMap<>(bound);
        if (includeSaves) {
            values.putAll(saved);
        }
        return values;
    }

    @Override
    public void close() {
        if (outer == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(outer);
        }
    }

    static Scope open(Map<Context<?>, Object> bound) {
        Scope scope = new Scope();
        if (scope.outer != null) {
            scope.bound.putAll(scope.outer.bound);
        }
        scope.bound.putAll(bound);
        CURRENT.set(scope);
        return scope;
    }

    static Snapshot snapshot(boolean includeSaves) {
        return current()
                .map(scope -> new Snapshot(scope.copy(includeSaves)))
                .orElseGet(() -> new Snapshot(Collections.emptyMap()));
    }

    @SuppressWarnings("unchecked")
    static <T> T getBoundValue(Context<T> context) {
        return (T) current()
                .map(scope -> scope.bound.get(context))
                .orElse(null);
    }

    static <T> T getSavedValue(Context<T> context, Supplier<? extends T> supplier) {
        return current()
                .orElseThrow(() -> new IllegalStateException("No active scope"))
                .save(context, supplier);
    }

    private static Optional<Scope> current() {
        return Optional.ofNullable(CURRENT.get());
    }
}
