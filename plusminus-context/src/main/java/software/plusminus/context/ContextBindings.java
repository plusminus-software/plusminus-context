package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ContextBindings {

    private final Map<ScopedContext<?>, Supplier<?>> suppliers;

    public <T> ContextBindings where(Context<T> context, Supplier<? extends T> supplier) {
        if (!(context instanceof ScopedContext)) {
            throw new IllegalArgumentException("Context cannot be bound");
        }
        Map<ScopedContext<?>, Supplier<?>> result = new HashMap<>(suppliers);
        result.put((ScopedContext<?>) context, supplier);
        return new ContextBindings(result);
    }

    public void run(Runnable body) {
        call(() -> {
            body.run();
            return null;
        });
    }

    public <R, X extends Throwable> R call(Call<R, X> body) throws X {
        Map<ScopedContext<?>, Lazy<?>> values = new HashMap<>(ScopedContext.values());
        suppliers.forEach((context, supplier) -> values.put(context, new Lazy<>(supplier)));
        return ScopedContext.call(values, body);
    }
}
