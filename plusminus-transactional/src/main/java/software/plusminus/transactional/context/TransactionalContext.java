package software.plusminus.transactional.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.plusminus.context.Context;

import java.util.Optional;
import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TransactionalContext<T> implements Context<T> {

    private final Supplier<T> supplier;

    public static <T> TransactionalContext<T> of(Supplier<T> supplier) {
        return new TransactionalContext<>(supplier);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<T> optional() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            return Optional.empty();
        }
        T value = (T) TransactionSynchronizationManager.getResource(this);
        if (value == null) {
            value = supplier.get();
            bind(value);
        }
        return Optional.ofNullable(value);
    }

    private void bind(T value) {
        if (value == null) {
            return;
        }
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
