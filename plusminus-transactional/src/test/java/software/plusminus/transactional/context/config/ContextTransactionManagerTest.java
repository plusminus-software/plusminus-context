package software.plusminus.transactional.context.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.plusminus.transactional.context.TransactionalContext;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.plusminus.check.Checks.check;

class ContextTransactionManagerTest {

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionalContext.CONTEXT.remove();
    }

    @Test
    void getResourceFactoryDelegatesToResourceTransactionManager() {
        ResourceTransactionManager delegate = mock(ResourceTransactionManager.class);
        Object resourceFactory = new Object();
        when(delegate.getResourceFactory()).thenReturn(resourceFactory);
        ContextTransactionManager manager = new ContextTransactionManager(delegate);

        assertSame(resourceFactory, manager.getResourceFactory());
    }

    @Test
    void getResourceFactoryFailsWhenDelegateIsNotResourceBased() {
        PlatformTransactionManager delegate = mock(PlatformTransactionManager.class);
        ContextTransactionManager manager = new ContextTransactionManager(delegate);

        IllegalStateException exception = assertThrows(IllegalStateException.class, manager::getResourceFactory);

        assertTrue(exception.getMessage().contains("is not a ResourceTransactionManager"));
        assertTrue(exception.getMessage().contains(delegate.getClass().getName()));
    }

    @Test
    void getTransactionCreatesContextFrameForNewTransaction() {
        TransactionSynchronizationManager.initSynchronization();
        PlatformTransactionManager delegate = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(status.isNewTransaction()).thenReturn(true);
        when(delegate.getTransaction(null)).thenReturn(status);
        ContextTransactionManager manager = new ContextTransactionManager(delegate);

        TransactionStatus result = manager.getTransaction(null);

        assertSame(status, result);
        check(TransactionalContext.CONTEXT.get()).hasSize(1);
    }

    @Test
    void getTransactionSkipsContextFrameForParticipatingTransaction() {
        PlatformTransactionManager delegate = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(status.isNewTransaction()).thenReturn(false);
        when(delegate.getTransaction(null)).thenReturn(status);
        ContextTransactionManager manager = new ContextTransactionManager(delegate);

        TransactionStatus result = manager.getTransaction(null);

        assertSame(status, result);
        check(TransactionalContext.CONTEXT.get()).isEmpty();
    }

    @Test
    void commitDelegates() {
        PlatformTransactionManager delegate = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        ContextTransactionManager manager = new ContextTransactionManager(delegate);

        manager.commit(status);

        verify(delegate).commit(status);
    }

    @Test
    void rollbackDelegates() {
        PlatformTransactionManager delegate = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        ContextTransactionManager manager = new ContextTransactionManager(delegate);

        manager.rollback(status);

        verify(delegate).rollback(status);
    }
}
