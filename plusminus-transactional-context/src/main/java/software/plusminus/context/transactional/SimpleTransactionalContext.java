package software.plusminus.context.transactional;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public class SimpleTransactionalContext<T> implements TransactionalContext<T> {

    private Supplier<T> provider;

    @Override
    public T provide() {
        return provider.get();
    }
}
