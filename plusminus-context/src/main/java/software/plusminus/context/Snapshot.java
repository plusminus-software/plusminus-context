package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Snapshot {

    private final Map<Context<?>, Object> values;

    public Runnable wrap(Runnable task) {
        Supplier<Void> body = () -> {
            task.run();
            return null;
        };
        return () -> Scope.call(values, body);
    }

    public <V> Callable<V> wrap(Callable<V> task) {
        return () -> Scope.call(values, task);
    }
}
