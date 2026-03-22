package software.plusminus.context.transactional.config;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import software.plusminus.context.transactional.TransactionalContext;

@AllArgsConstructor
public class ContextTransactionManager implements PlatformTransactionManager {

    private PlatformTransactionManager delegate;

    @Override
    public TransactionStatus getTransaction(@Nullable TransactionDefinition definition) {
        TransactionStatus status = delegate.getTransaction(definition);
        if (status.isNewTransaction()) {
            TransactionalContext.onNewTransaction();
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
