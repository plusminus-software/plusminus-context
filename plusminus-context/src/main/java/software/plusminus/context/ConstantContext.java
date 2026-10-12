package software.plusminus.context;

import lombok.AllArgsConstructor;

@AllArgsConstructor
final class ConstantContext<T> implements Context<T> {

    private final T constant;

    @Override
    public T get() {
        T value = Scope.getBoundValue(this);
        return value == null ? constant : value;
    }
}
