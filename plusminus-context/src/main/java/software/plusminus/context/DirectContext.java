package software.plusminus.context;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
final class DirectContext<T> implements Context<T> {

    private final Supplier<T> supplier;

    @Override
    public T get() {
        T value = Scope.getBoundValue(this);
        return value == null ? supplier.get() : value;
    }
}
