package software.plusminus.context;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

final class ThreadLocalScope implements Scope, AutoCloseable {

    static final ThreadLocal<ThreadLocalScope> CURRENT = new ThreadLocal<>();

    @Nullable
    private final ThreadLocalScope outer;
    private final Map<Context<?>, Object> bound;
    private final Map<Context<?>, Object> cached = new HashMap<>();

    private ThreadLocalScope(@Nullable ThreadLocalScope outer, Map<Context<?>, Object> bindings) {
        this.outer = outer;
        this.bound = outer == null ? new HashMap<>() : new HashMap<>(outer.bound);
        this.bound.putAll(bindings);
    }

    @Override
    public <T> boolean contains(Context<T> context) {
        return bound.containsKey(context) || cached.containsKey(context);
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Context<T> context) {
        if (bound.containsKey(context)) {
            return (T) bound.get(context);
        }
        return (T) cached.get(context);
    }

    @Override
    public <T> void set(Context<T> context, @Nullable T value) {
        cached.put(context, value);
    }

    /**
     * Returns a copy of the bound values.
     */
    Map<Context<?>, Object> bindings() {
        return new HashMap<>(bound);
    }

    @Override
    public void close() {
        if (outer == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(outer);
        }
    }

    static ThreadLocalScope open(Map<Context<?>, Object> bindings) {
        ThreadLocalScope scope = new ThreadLocalScope(CURRENT.get(), bindings);
        CURRENT.set(scope);
        return scope;
    }
}
