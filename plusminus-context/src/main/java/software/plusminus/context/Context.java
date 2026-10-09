package software.plusminus.context;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Context<T> {

    Optional<T> optional();

    default T get() {
        return optional().orElseThrow(() -> new NoSuchElementException("Context is not bound"));
    }

    default <R> Context<R> map(Function<? super T, ? extends R> mapper) {
        return () -> optional().map(mapper);
    }

    static <T> Context<T> of() {
        return new ScopedContext<>(false);
    }

    static <T> Context<T> of(Supplier<T> supplier) {
        return () -> Optional.ofNullable(supplier.get());
    }

    static <T> Context<T> local() {
        return new ScopedContext<>(true);
    }

    static <T> Context<T> constant(T value) {
        return () -> Optional.ofNullable(value);
    }

    static <T> ContextBindings where(Context<T> context, Supplier<? extends T> supplier) {
        return new ContextBindings(Collections.emptyMap()).where(context, supplier);
    }

    static Snapshot snapshot() {
        return new Snapshot(ScopedContext.values());
    }
}
