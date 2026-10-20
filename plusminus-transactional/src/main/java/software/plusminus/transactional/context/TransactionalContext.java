package software.plusminus.transactional.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.plusminus.context.Context;

import java.util.function.Supplier;

public final class TransactionalContext {

    private TransactionalContext() {
    }

    /**
     * Creates a context with one value per active transaction. The value is not cached in the scope,
     * and a value bound with {@code where} replaces it even outside a transaction.
     */
    public static <T> Context<T> of(Supplier<? extends T> supplier) {
        return Context.dynamic(new TransactionalSupplier<>(supplier));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TransactionalSupplier<T> implements Supplier<T> {

        private final Supplier<? extends T> supplier;

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
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
                    TransactionSynchronizationManager.unbindResource(TransactionalSupplier.this);
                }

                @Override
                public void resume() {
                    TransactionSynchronizationManager.bindResource(TransactionalSupplier.this, value);
                }

                @Override
                public void afterCompletion(int status) {
                    TransactionSynchronizationManager.unbindResourceIfPossible(TransactionalSupplier.this);
                }
            });
        }
    }
}
