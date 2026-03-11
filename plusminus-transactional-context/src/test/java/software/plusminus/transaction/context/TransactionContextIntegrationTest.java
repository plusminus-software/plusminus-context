package software.plusminus.transaction.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.plusminus.transaction.context.fixtures.TransactionalService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.plusminus.check.Checks.check;

@SpringBootTest
class TransactionContextIntegrationTest {

    public static final String TEST_VALUE = "testValue";

    @Autowired
    private TransactionalService transactionalService;
    private AtomicInteger index = new AtomicInteger(1);
    private TransactionContext<String> transactionContext = TransactionContext.of(
            () -> TEST_VALUE + index.getAndIncrement());

    @Test
    void noTransaction() {
        checkContextOutsideTransaction();
    }

    @Test
    void singleTransaction() {
        transactionalService.inTransaction(() -> checkContext(1));
        checkContextOutsideTransaction();
    }

    @Test
    void joinedTransactions() {
        transactionalService.inTransaction(() -> {
            checkContext(1);
            transactionalService.inTransaction(() -> checkContext(1));
            checkContext(1);
        });
        checkContextOutsideTransaction();
    }

    @Test
    void nestedNewTransactions() {
        transactionalService.inTransaction(() -> {
            checkContext(1);
            transactionalService.inNewTransaction(() -> {
                checkContext(2);
                transactionalService.inTransaction(() -> {
                    checkContext(2);
                    transactionalService.inNewTransaction(() -> checkContext(3));
                    checkContext(2);
                });
                checkContext(2);
            });
            checkContext(1);
        });
        checkContextOutsideTransaction();
    }

    private void checkContext(int transactionLevel) {
        check(TransactionContext.CONTEXT.get()).hasSize(transactionLevel);
        String value = transactionContext.get();
        check(value).is(TEST_VALUE + transactionLevel);
    }

    private void checkContextOutsideTransaction() {
        List<Map<TransactionContext<?>, Object>> context = TransactionContext.CONTEXT.get();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transactionContext.get());

        check(context).isEmpty();
        check(exception.getMessage()).is("No active transaction");
    }
}