package software.plusminus.context.propagation.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import software.plusminus.context.propagation.ContextPropagationTaskDecorator;

@Configuration
public class ContextPropagationAutoconfig {

    @Bean
    @ConditionalOnMissingBean(TaskDecorator.class)
    public TaskDecorator contextPropagationTaskDecorator() {
        return new ContextPropagationTaskDecorator();
    }

    @Bean
    public static BeanPostProcessor contextPropagationPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof TaskDecorator && !(bean instanceof ContextPropagationTaskDecorator)) {
                    return new ContextPropagationTaskDecorator((TaskDecorator) bean);
                }
                return bean;
            }
        };
    }
}
