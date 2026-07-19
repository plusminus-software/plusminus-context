package software.plusminus.context;

import java.util.function.Supplier;

public interface WritableContext<T> extends Context<T> {

    void set(T value);

    void replace(T value);

    static <T> WritableContext<T> of() {
        return of(false);
    }

    static <T> WritableContext<T> of(boolean inheritable) {
        ThreadLocal<T> threadLocal = inheritable ? new InheritableThreadLocal<>() : new ThreadLocal<>();
        return new WritableThreadLocalContext<>(threadLocal);
    }

    static <T> WritableContext<T> of(T initialValue) {
        return of(initialValue, false);
    }

    static <T> WritableContext<T> of(T initialValue, boolean inheritable) {
        return of(() -> initialValue, inheritable);
    }

    static <T> WritableContext<T> of(Supplier<T> supplier) {
        return of(supplier, false);
    }

    static <T> WritableContext<T> of(Supplier<T> supplier, boolean inheritable) {
        ThreadLocal<T> threadLocal = ThreadLocalUtil.createThreadLocal(supplier, inheritable);
        return new WritableThreadLocalContext<>(threadLocal);
    }
}
