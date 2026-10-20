package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class DynamicContext<T> implements UncachedContext<T> {

    private final Supplier<T> supplier;

    @Override
    public T supply() {
        return supplier.get();
    }

}
