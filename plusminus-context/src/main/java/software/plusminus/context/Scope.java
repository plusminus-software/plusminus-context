package software.plusminus.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings("PMD.CloseResource")
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
            saved.put(context, value);
        }
        return (T) value;
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
        Scope scope = CURRENT.get();
        if (scope == null) {
            return new Snapshot(Collections.emptyMap());
        }
        Map<Context<?>, Object> values = new HashMap<>(scope.bound);
        if (includeSaves) {
            values.putAll(scope.saved);
        }
        return new Snapshot(values);
    }

    @SuppressWarnings("unchecked")
    static <T> T getBoundValue(Context<T> context) {
        Scope scope = getScope();
        return (T) scope.bound.get(context);
    }

    static <T> T getSavedValue(Context<T> context, Supplier<? extends T> supplier) {
        Scope scope = getScope();
        return scope.save(context, supplier);
    }

    private static Scope getScope() {
        Scope scope = CURRENT.get();
        if (scope == null) {
            throw new IllegalStateException("Scope is not initialized");
        }
        return scope;
    }
}
