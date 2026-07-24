package software.plusminus.transactional.context;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface TransactionalContext<T> {

    ThreadLocal<List<Map<TransactionalContext<?>, Object>>> CONTEXT = ThreadLocal.withInitial(ArrayList::new);

    default T get() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()
                || !TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction");
        }
        List<Map<TransactionalContext<?>, Object>> transactions = CONTEXT.get();
        if (transactions.isEmpty()) {
            throw new IllegalStateException("Cannot get transaction context");
        }
        Map<TransactionalContext<?>, Object> values = transactions.get(transactions.size() - 1);
        return (T) values.computeIfAbsent(this, self -> provide());
    }

    T provide();

    static <T> TransactionalContext<T> of(Supplier<T> provider) {
        return new SimpleTransactionalContext<>(provider);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    static void onNewTransaction() {
        List<Map<TransactionalContext<?>, Object>> transactions = CONTEXT.get();
        Map<TransactionalContext<?>, Object> transaction = new HashMap<>();
        transactions.add(transaction);
        try {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    List<Map<TransactionalContext<?>, Object>> current = CONTEXT.get();
                    // Remove exactly the frame this synchronization owns (by identity), so an
                    // out-of-order completion cannot pop a sibling transaction's frame.
                    for (int i = current.size() - 1; i >= 0; i--) {
                        if (current.get(i) == transaction) {
                            current.remove(i);
                            break;
                        }
                    }
                    if (current.isEmpty()) {
                        CONTEXT.remove();
                    }
                }
            });
        } catch (RuntimeException e) {
            transactions.remove(transactions.size() - 1);
            if (transactions.isEmpty()) {
                CONTEXT.remove();
            }
            throw e;
        }
    }
}
