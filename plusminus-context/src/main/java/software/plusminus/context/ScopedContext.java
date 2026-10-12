package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class ScopedContext<T> implements Context<T> {

    private final Supplier<T> supplier;

    @Override
    public T supply() {
        return Scope.getSavedValue(this, supplier);
    }
}
