package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ContextBindings {

    private final Map<Context<?>, Object> values;

    public <T> ContextBindings where(Context<T> context, T value) {
        Map<Context<?>, Object> result = new HashMap<>(values);
        result.put(context, Objects.requireNonNull(value));
        return new ContextBindings(result);
    }

    public void run(Runnable body) {
        call(() -> {
            body.run();
            return null;
        });
    }

    public <R> R call(Supplier<R> body) {
        try (Scope ignored = Scope.open(values)) {
            return body.get();
        }
    }
}
