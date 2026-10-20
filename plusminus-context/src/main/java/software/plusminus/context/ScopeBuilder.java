package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class ScopeBuilder {

    private final Map<Context<?>, Object> bindings;

    ScopeBuilder() {
        this(Collections.emptyMap());
    }

    public <T> ScopeBuilder where(Context<T> context, @Nullable T value) {
        Map<Context<?>, Object> result = new HashMap<>(bindings);
        result.put(context, value);
        return new ScopeBuilder(result);
    }

    public <T> ScopeBuilder where(Context<T> context) {
        return where(context, context.get());
    }

    public void run(Runnable body) {
        call(() -> {
            body.run();
            return null;
        });
    }

    public <R> R call(Supplier<R> body) {
        return Scope.call(bindings, body);
    }
}
