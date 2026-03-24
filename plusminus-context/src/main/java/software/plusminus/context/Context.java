package software.plusminus.context;

import java.util.Optional;
import java.util.function.Supplier;

public interface Context<T> {

    T get();

    default Optional<T> optional() {
        return Optional.ofNullable(get());
    }

    boolean isInheritable();

    static <T> Context<T> constant(T value) {
        return new ConstantContext<>(value);
    }

    static <T> Context<T> of(T initialValue) {
        return of(initialValue, true);
    }

    static <T> Context<T> of(T initialValue, boolean inheritable) {
        return of(() -> initialValue, inheritable);
    }

    static <T> Context<T> of(Supplier<T> supplier) {
        return of(supplier, true);
    }

    static <T> Context<T> of(Supplier<T> supplier, boolean inheritable) {
        ThreadLocal<T> threadLocal = ThreadLocalUtil.createThreadLocal(supplier, inheritable);
        return new ThreadLocalContext<>(threadLocal);
    }
}
