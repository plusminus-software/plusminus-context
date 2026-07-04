package software.plusminus.transactional.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.plusminus.transactional.context.fixtures.TransactionService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static software.plusminus.check.Checks.check;

@SpringBootTest
class TransactionalContextIntegrationTest {

    public static final String TEST_VALUE = "testValue";

    @Autowired
    private TransactionService transactionService;
    private AtomicInteger index = new AtomicInteger(1);
    private TransactionalContext<String> transactionalContext = TransactionalContext.of(
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

    private void checkContext(int transactionLevel) {
        check(TransactionalContext.CONTEXT.get()).hasSize(transactionLevel);
        String value = transactionalContext.get();
        check(value).is(TEST_VALUE + transactionLevel);
    }

    private void checkContextOutsideTransaction() {
        List<Map<TransactionalContext<?>, Object>> context = TransactionalContext.CONTEXT.get();
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> transactionalContext.get());

        check(context).isEmpty();
        check(exception.getMessage()).is("No active transaction");
    }
}