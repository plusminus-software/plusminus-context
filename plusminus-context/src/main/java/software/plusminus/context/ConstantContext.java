package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class ConstantContext<T> implements UncachedContext<T> {

    private final T constant;

    @Override
    public T supply() {
        return constant;
    }

}
