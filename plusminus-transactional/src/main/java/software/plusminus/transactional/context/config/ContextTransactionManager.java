package software.plusminus.transactional.context.config;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import software.plusminus.transactional.context.TransactionalContext;

@AllArgsConstructor
public class ContextTransactionManager implements ResourceTransactionManager {

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

    @Override
    public Object getResourceFactory() {
        if (delegate instanceof ResourceTransactionManager) {
            return ((ResourceTransactionManager) delegate).getResourceFactory();
        }
        throw new IllegalStateException("Wrapped transaction manager "
                + delegate.getClass().getName() + " is not a ResourceTransactionManager");
    }
}
