package software.plusminus.context;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Values of contexts for the current thread.
 *
 * <p>A scope has two kinds of values: bound values from {@link ScopeBuilder#where} and cached values
 * from {@link #set}. Inner scopes and snapshots get only bound values.
 */
interface Scope {

    <T> boolean contains(Context<T> context);

    @Nullable
    <T> T get(Context<T> context);

    /**
     * Caches the value in this scope only.
     */
    <T> void set(Context<T> context, @Nullable T value);

    @Nullable
    static Scope current() {
        return ThreadLocalScope.CURRENT.get();
    }

    /**
     * Runs the body in a new inner scope with the given bound values.
     */
    static <R> R call(Map<Context<?>, Object> bindings, Supplier<R> body) {
        try (ThreadLocalScope ignored = ThreadLocalScope.open(bindings)) {
            return body.get();
        }
    }

    /**
     * Runs the body in a new inner scope with the given bound values.
     */
    static <R> R call(Map<Context<?>, Object> bindings, Callable<R> body) throws Exception {
        try (ThreadLocalScope ignored = ThreadLocalScope.open(bindings)) {
            return body.call();
        }
    }

    static Snapshot snapshot() {
        ThreadLocalScope scope = ThreadLocalScope.CURRENT.get();
        return new Snapshot(scope == null ? Collections.emptyMap() : scope.bindings());
    }
}
