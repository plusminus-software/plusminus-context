package software.plusminus.context;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@AllArgsConstructor
final class ScopedContext<T> implements Context<T> {

    private static final ThreadLocal<Map<ScopedContext<?>, Lazy<?>>> CURRENT =
            ThreadLocal.withInitial(Collections::emptyMap);

    @Getter
    private final boolean local;

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> optional() {
        Lazy<T> value = (Lazy<T>) CURRENT.get().get(this);
        return Optional.ofNullable(value).map(Lazy::get);
    }

    static Map<ScopedContext<?>, Lazy<?>> values() {
        return CURRENT.get();
    }

    static <R, X extends Throwable> R call(Map<ScopedContext<?>, Lazy<?>> values, Call<R, X> body) throws X {
        Map<ScopedContext<?>, Lazy<?>> previous = CURRENT.get();
        CURRENT.set(values);
        try {
            return body.call();
        } finally {
            CURRENT.set(previous);
        }
    }
}
