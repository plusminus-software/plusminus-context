package software.plusminus.context;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
final class Lazy<T> {

    private Supplier<? extends T> supplier;
    private T value;

    Lazy(Supplier<? extends T> supplier) {
        this(supplier, null);
    }

    synchronized T get() {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;
        }
        return value;
    }
}
