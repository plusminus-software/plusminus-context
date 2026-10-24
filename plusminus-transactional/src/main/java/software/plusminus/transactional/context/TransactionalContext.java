package software.plusminus.transactional.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.plusminus.context.Context;

import java.util.Objects;
import java.util.function.Supplier;

public final class TransactionalContext {

    private TransactionalContext() {
    }

    /**
     * Creates a context with one value per active transaction. The value is not cached in the scope,
     * and a value bound with {@code where} replaces it even outside a transaction.
     */
    public static <T> Context<T> of(Supplier<? extends T> supplier) {
        return Context.dynamic(new TransactionalSupplier<>(Objects.requireNonNull(supplier)));
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TransactionalSupplier<T> implements Supplier<T> {

        private static final Object NULL = new Object();

        private final Supplier<? extends T> supplier;

        @Override
        @SuppressWarnings("unchecked")
        public T get() {
            if (!TransactionSynchronizationManager.isSynchronizationActive()
                    || !TransactionSynchronizationManager.isActualTransactionActive()) {
                throw new IllegalStateException("No active transaction");
            }
            Object resource = TransactionSynchronizationManager.getResource(this);
            if (resource == null) {
                T value = supplier.get();
                bind(value == null ? NULL : value);
                return value;
            }
            return resource == NULL ? null : (T) resource;
        }

        private void bind(Object value) {
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
