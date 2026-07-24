package software.plusminus.transactional.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.plusminus.check.Checks.check;

class TransactionalContextTest {

    @BeforeEach
    void setUp() {
        TransactionalContext.CONTEXT.remove();
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
        }
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
        TransactionalContext.CONTEXT.remove();
    }

    @Test
    void completionRemovesOwnFrameAndClearsContext() {
        TransactionalContext.onNewTransaction();
        check(TransactionalContext.CONTEXT.get()).hasSize(1);
        TransactionSynchronization synchronization = TransactionSynchronizationManager.getSynchronizations().get(0);

        synchronization.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

        check(TransactionalContext.CONTEXT.get()).isEmpty();
    }

    @Test
    void outOfOrderCompletionRemovesOwnFrameOnly() {
        TransactionalContext.onNewTransaction();
        TransactionalContext.onNewTransaction();
        Map<TransactionalContext<?>, Object> innerFrame = TransactionalContext.CONTEXT.get().get(1);
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();

        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

        List<Map<TransactionalContext<?>, Object>> current = TransactionalContext.CONTEXT.get();
        check(current).hasSize(1);
        assertSame(innerFrame, current.get(0));

        synchronizations.get(1).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        check(TransactionalContext.CONTEXT.get()).isEmpty();
    }

    @Test
    void staleCompletionDoesNotRemoveSiblingFrame() {
        TransactionalContext.onNewTransaction();
        TransactionalContext.onNewTransaction();
        Map<TransactionalContext<?>, Object> innerFrame = TransactionalContext.CONTEXT.get().get(1);
        List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();

        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        synchronizations.get(0).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);

        List<Map<TransactionalContext<?>, Object>> current = TransactionalContext.CONTEXT.get();
        check(current).hasSize(1);
        assertSame(innerFrame, current.get(0));
    }

    @Test
    void failedRegistrationRemovesFrameAndClearsContext() {
        TransactionSynchronizationManager.clearSynchronization();

        assertThrows(IllegalStateException.class, TransactionalContext::onNewTransaction);

        check(TransactionalContext.CONTEXT.get()).isEmpty();
    }

    @Test
    void failedRegistrationKeepsOuterFrames() {
        TransactionalContext.onNewTransaction();
        Map<TransactionalContext<?>, Object> outerFrame = TransactionalContext.CONTEXT.get().get(0);
        TransactionSynchronizationManager.clearSynchronization();

        assertThrows(IllegalStateException.class, TransactionalContext::onNewTransaction);

        List<Map<TransactionalContext<?>, Object>> current = TransactionalContext.CONTEXT.get();
        check(current).hasSize(1);
        assertSame(outerFrame, current.get(0));
    }
}
