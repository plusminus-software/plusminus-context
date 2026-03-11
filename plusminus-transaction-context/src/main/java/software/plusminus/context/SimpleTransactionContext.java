package software.plusminus.context;

import lombok.AllArgsConstructor;

import java.util.function.Supplier;

@AllArgsConstructor
public class SimpleTransactionContext<T> implements TransactionContext<T> {

    private Supplier<T> provider;

    @Override
    public T provide() {
        return provider.get();
    }
}
