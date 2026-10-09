package software.plusminus.context;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public final class Snapshot {

    private final Map<ScopedContext<?>, Lazy<?>> values;

    Snapshot(Map<ScopedContext<?>, Lazy<?>> values) {
        this.values = new HashMap<>(values);
        this.values.keySet().removeIf(ScopedContext::isLocal);
    }

    public Runnable wrap(Runnable task) {
        return () -> ScopedContext.call(values, () -> {
            task.run();
            return null;
        });
    }

    public <V> Callable<V> wrap(Callable<V> task) {
        return () -> ScopedContext.call(values, task::call);
    }
}
