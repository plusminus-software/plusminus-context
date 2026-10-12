package software.plusminus.context;

import lombok.AllArgsConstructor;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

@AllArgsConstructor
final class DirectContext<T> implements Context<T> {

    private final Supplier<T> supplier;

    @Override
    public T get() {
        T value = Scope.getBoundValue(this);
        if (value == null) {
            value = supplier.get();
        }
        if (value == null) {
            throw new NoSuchElementException("No value in context");
        }
        return value;
    }
}
