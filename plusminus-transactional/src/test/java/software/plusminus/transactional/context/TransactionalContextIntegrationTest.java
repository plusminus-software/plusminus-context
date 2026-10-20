package software.plusminus.transactional.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import software.plusminus.context.Context;
import software.plusminus.transactional.context.fixtures.TransactionService;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.plusminus.check.Checks.check;

@SpringBootTest
class TransactionalContextIntegrationTest {

    public static final String TEST_VALUE = "testValue";

    @Autowired
    private TransactionService transactionService;
    private AtomicInteger index = new AtomicInteger(1);
    private Context<String> transactionalContext = TransactionalContext.of(
            () -> TEST_VALUE + index.getAndIncrement());

    @Test
    void noTransaction() {
        checkContextOutsideTransaction();
    }

    @Test
    void singleTransaction() {
        transactionService.inTransaction(() -> checkContext(1));
        checkContextOutsideTransaction();
    }

    @Test
    void whereReplacesTransactionValue() {
        transactionService.inTransaction(() -> {
            Context.where(transactionalContext, "bound")
                    .run(() -> check(transactionalContext.get()).is("bound"));
            checkContext(1);
        });
    }

    @Test
    void whereWorksOutsideTransaction() {
        Context.where(transactionalContext, "bound")
                .run(() -> check(transactionalContext.get()).is("bound"));
        checkContextOutsideTransaction();
    }

    @Test
    void joinedTransactions() {
        transactionService.inTransaction(() -> {
            checkContext(1);
            transactionService.inTransaction(() -> checkContext(1));
            checkContext(1);
        });
        checkContextOutsideTransaction();
    }

    @Test
    void nestedNewTransactions() {
        transactionService.inTransaction(() -> {
            checkContext(1);
            transactionService.inNewTransaction(() -> {
                checkContext(2);
                transactionService.inTransaction(() -> {
                    checkContext(2);
                    transactionService.inNewTransaction(() -> checkContext(3));
                    checkContext(2);
                });
                checkContext(2);
            });
            checkContext(1);
        });
        checkContextOutsideTransaction();
    }

    @Test
    void newTransactionWithoutValueInOuterTransaction() {
        transactionService.inTransaction(() -> {
            transactionService.inNewTransaction(() -> checkContext(1));
            checkContext(2);
        });
        checkContextOutsideTransaction();
    }

    @Test
    void transactionWithoutSynchronization() {
        transactionService.inTransaction(() -> {
            TransactionSynchronizationManager.clearSynchronization();
            try {
                assertThrows(IllegalStateException.class, () -> transactionalContext.get());
            } finally {
                TransactionSynchronizationManager.initSynchronization();
            }
        });
    }

    @Test
    void supplierReadsOtherTransactionalContext() {
        Context<String> derived = TransactionalContext.of(() -> transactionalContext.get() + "!");

        transactionService.inTransaction(() -> check(derived.get()).is(TEST_VALUE + "1!"));
    }

    @Test
    void nullValue() {
        Context<String> nullContext = TransactionalContext.of(() -> null);

        transactionService.inTransaction(() -> assertNull(nullContext.get()));
    }

    private void checkContext(int transactionLevel) {
        check(transactionalContext.get()).is(TEST_VALUE + transactionLevel);
    }

    private void checkContextOutsideTransaction() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> transactionalContext.get());
        check(exception.getMessage()).is("No active transaction");
        check(TransactionSynchronizationManager.getResourceMap().isEmpty()).is(true);
    }
}
