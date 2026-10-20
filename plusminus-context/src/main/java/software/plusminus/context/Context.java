package software.plusminus.context;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * A key for a value in the current scope.
 *
 * <p>A scope finds a value only by the same context instance, so create each context once:
 * as a {@code static final} field or a Spring bean.
 */
@FunctionalInterface
public interface Context<T> {

    T supply();

    default T get() {
        Scope scope = Scope.current();
        if (scope == null) {
            return supply();
        }
        if (scope.contains(this)) {
            return scope.get(this);
        }
        T value = supply();
        scope.set(this, value);
        return value;
    }

    default Optional<T> optional() {
        return Optional.ofNullable(get());
    }

    default <R> Context<R> map(Function<? super T, ? extends R> mapper) {
        return dynamic(() -> mapper.apply(get()));
    }

    static <T> Context<T> of(Supplier<T> supplier) {
        return new SupplierContext<>(supplier);
    }

    static <T> Context<T> constant(@Nullable T value) {
        return new ConstantContext<>(value);
    }

    static <T> Context<T> dynamic(Supplier<T> supplier) {
        return new DynamicContext<>(supplier);
    }

    static <T> Context<T> required() {
        return new RequiredContext<>();
    }

    static <T> ScopeBuilder where(Context<T> context, @Nullable T value) {
        return new ScopeBuilder().where(context, value);
    }

    static <T> ScopeBuilder where(Context<T> context) {
        return new ScopeBuilder().where(context);
    }

    static void run(Runnable body) {
        new ScopeBuilder().run(body);
    }

    static <R> R call(Supplier<R> body) {
        return new ScopeBuilder().call(body);
    }

    static Snapshot snapshot() {
        return Scope.snapshot();
    }
}
