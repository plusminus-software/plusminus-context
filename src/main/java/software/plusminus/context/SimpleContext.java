package software.plusminus.context;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public final class SimpleContext<T> implements Context<T> {

    private Supplier<T> provider;

    @Override
    public T provide() {
        return provider.get();
    }

}
