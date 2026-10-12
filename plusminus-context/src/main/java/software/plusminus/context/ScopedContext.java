package software.plusminus.context;

import java.util.function.Supplier;

public class ScopedContext<T> implements Context<T> {

    private final Supplier<T> supplier;

    ScopedContext(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        T value = Scope.getBoundValue(this);
        if (value != null) {
            return value;
        }
        value = Scope.getSavedValue(this, supplier);
        return value;
    }
}
