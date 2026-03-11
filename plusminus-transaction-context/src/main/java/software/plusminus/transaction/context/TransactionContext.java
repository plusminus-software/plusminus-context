package software.plusminus.transaction.context;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface TransactionContext<T> {

    ThreadLocal<List<Map<TransactionContext<?>, Object>>> CONTEXT = ThreadLocal.withInitial(ArrayList::new);

    default T get() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("No active transaction");
        }
        List<Map<TransactionContext<?>, Object>> transactions = CONTEXT.get();
        if (transactions.isEmpty()) {
            throw new IllegalStateException("Cannot get transaction context");
        }
        Map<TransactionContext<?>, Object> values = transactions.get(transactions.size() - 1);
        return (T) values.computeIfAbsent(this, self -> provide());
    }

    T provide();

    static <T> TransactionContext<T> of(Supplier<T> provider) {
        return new SimpleTransactionContext<>(provider);
    }

    static void onNewTransaction() {
        CONTEXT.get().add(new HashMap<>());
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                List<Map<TransactionContext<?>, Object>> transactions = CONTEXT.get();
                transactions.remove(transactions.size() - 1);
            }
        });
    }
}
