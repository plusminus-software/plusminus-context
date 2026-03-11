package software.plusminus.transaction.context.config;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import software.plusminus.transaction.context.TransactionContext;

@AllArgsConstructor
public class ContextTransactionManager implements PlatformTransactionManager {

    private PlatformTransactionManager delegate;

    @Override
    public TransactionStatus getTransaction(@Nullable TransactionDefinition definition) {
        TransactionStatus status = delegate.getTransaction(definition);
        if (status.isNewTransaction()) {
            TransactionContext.onNewTransaction();
        }
        return status;
    }

    @Override
    public void commit(TransactionStatus status) throws TransactionException {
        delegate.commit(status);
    }

    @Override
    public void rollback(TransactionStatus status) throws TransactionException {
        delegate.rollback(status);
    }
}
