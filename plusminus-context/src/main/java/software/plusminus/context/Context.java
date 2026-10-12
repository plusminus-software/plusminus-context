package software.plusminus.context;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@FunctionalInterface
public interface Context<T> {

    T provide();

    default T get() {
        T value = Scope.getBoundValue(this);
        if (value == null) {
            value = provide();
        }
        if (value == null) {
            throw new NoSuchElementException("No value in context");
        }
        return value;
    }

    default <R> Context<R> map(Function<? super T, ? extends R> mapper) {
        return () -> mapper.apply(get());
    }

    static <T> Context<T> of(Supplier<T> supplier) {
        return new ScopedContext<>(supplier);
    }

    static <T> Context<T> of(T value) {
        Objects.requireNonNull(value);
        return () -> value;
    }

    static <T> Context<T> of() {
        return new ScopedContext<>(() -> null);
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
