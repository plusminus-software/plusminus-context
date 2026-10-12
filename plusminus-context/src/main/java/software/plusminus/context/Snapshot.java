package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.concurrent.Callable;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class Snapshot {

    private final Map<Context<?>, Object> values;

    public Runnable wrap(Runnable task) {
        return () -> {
            try (Scope ignored = Scope.open(values)) {
                task.run();
            }
        };
    }

    public <V> Callable<V> wrap(Callable<V> task) {
        return () -> {
            try (Scope ignored = Scope.open(values)) {
                return task.call();
            }
        };
    }
}
