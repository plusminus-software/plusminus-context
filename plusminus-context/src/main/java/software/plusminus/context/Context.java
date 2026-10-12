package software.plusminus.context;

import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Context<T> {

    T get();

    default <R> Context<R> map(Function<? super T, ? extends R> mapper) {
        return new ScopedContext<>(() -> mapper.apply(get()));
    }

    static <T> Context<T> of(Supplier<T> supplier) {
        return new ScopedContext<>(supplier);
    }

    static <T> Context<T> of(T value) {
        return new ConstantContext<>(value);
    }

    static <T> Context<T> of() {
        return new ScopedContext<>(() -> {
            throw new IllegalStateException("Context is not bound");
        });
    }

    static <T> ContextBindings where(Context<T> context, T value) {
        return new ContextBindings(Collections.emptyMap()).where(context, value);
    }

    static void run(Runnable body) {
        new ContextBindings(Collections.emptyMap()).run(body);
    }

    static <R> R call(Supplier<R> body) {
        return new ContextBindings(Collections.emptyMap()).call(body);
    }

    static Snapshot snapshot() {
        return snapshot(false);
    }

    static Snapshot snapshot(boolean includeSaves) {
        return Scope.snapshot(includeSaves);
    }
}
