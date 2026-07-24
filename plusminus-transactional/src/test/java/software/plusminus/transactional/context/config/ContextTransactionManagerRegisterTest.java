package software.plusminus.transactional.context.config;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.CallbackPreferringPlatformTransactionManager;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ContextTransactionManagerRegisterTest {

    private ContextTransactionManagerRegister register = new ContextTransactionManagerRegister();

    @Test
    void wrapsPlatformTransactionManager() {
        PlatformTransactionManager bean = mock(PlatformTransactionManager.class);

        Object result = register.postProcessAfterInitialization(bean, "transactionManager");

        assertTrue(result instanceof ContextTransactionManager);
        TransactionStatus status = mock(TransactionStatus.class);
        ((PlatformTransactionManager) result).commit(status);
        verify(bean).commit(status);
    }

    @Test
    void keepsContextTransactionManagerUnwrapped() {
        ContextTransactionManager bean = new ContextTransactionManager(mock(PlatformTransactionManager.class));

        Object result = register.postProcessAfterInitialization(bean, "transactionManager");

        assertSame(bean, result);
    }

    @Test
    void keepsCallbackPreferringTransactionManagerUnwrapped() {
        CallbackPreferringPlatformTransactionManager bean = mock(CallbackPreferringPlatformTransactionManager.class);

        Object result = register.postProcessAfterInitialization(bean, "transactionManager");

        assertSame(bean, result);
    }

    @Test
    void keepsOtherBeansUntouched() {
        Object bean = new Object();

        Object result = register.postProcessAfterInitialization(bean, "someBean");

        assertSame(bean, result);
    }
}
