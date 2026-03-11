package software.plusminus.transaction.context.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@ConditionalOnClass(PlatformTransactionManager.class)
public class ContextTransactionManagerRegister implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof PlatformTransactionManager && !(bean instanceof ContextTransactionManagerRegister)) {
            return new ContextTransactionManager((PlatformTransactionManager) bean);
        }
        return bean;
    }
}
