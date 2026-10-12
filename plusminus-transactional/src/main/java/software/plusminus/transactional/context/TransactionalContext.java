package software.plusminus.transactional.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.plusminus.context.Context;

import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionalContext<T> implements Context<T> {

    private final Supplier<? extends T> supplier;

    public static <T> TransactionalContext<T> of(Supplier<? extends T> supplier) {
        return new TransactionalContext<>(supplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T supply() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction");
        }
        T value = (T) TransactionSynchronizationManager.getResource(this);
        if (value == null) {
            value = supplier.get();
            if (value != null) {
                bind(value);
            }
        }
        return value;
    }

    private void bind(T value) {
        TransactionSynchronizationManager.bindResource(this, value);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void suspend() {
                TransactionSynchronizationManager.unbindResource(TransactionalContext.this);
            }

            @Override
            public void resume() {
                TransactionSynchronizationManager.bindResource(TransactionalContext.this, value);
            }

            @Override
            public void afterCompletion(int status) {
                TransactionSynchronizationManager.unbindResourceIfPossible(TransactionalContext.this);
            }
        });
    }
}
