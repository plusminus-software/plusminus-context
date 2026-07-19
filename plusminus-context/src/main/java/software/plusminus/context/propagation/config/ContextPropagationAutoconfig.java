package software.plusminus.context.propagation.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.core.type.AnnotatedTypeMetadata;
import software.plusminus.context.Context;
import software.plusminus.context.propagation.ContextPropagationTaskDecorator;

import java.util.stream.Collectors;

@Configuration
@Conditional(ContextPropagationAutoconfig.OnNoTaskDecoratorComposition.class)
public class ContextPropagationAutoconfig {

    @Bean
    @ConditionalOnMissingBean(TaskDecorator.class)
    public TaskDecorator contextPropagationTaskDecorator(ObjectProvider<Context<?>> contexts) {
        return new ContextPropagationTaskDecorator(contexts.stream().collect(Collectors.toList()));
    }

    @Bean
    public static BeanPostProcessor taskDecoratorPropagationPostProcessor(ObjectProvider<Context<?>> contexts) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof TaskDecorator && !(bean instanceof ContextPropagationTaskDecorator)) {
                    return new ContextPropagationTaskDecorator(
                            contexts.stream().collect(Collectors.toList()), (TaskDecorator) bean);
                }
                return bean;
            }
        };
    }

    @Bean
    public SmartInitializingSingleton taskDecoratorDuplicationCheck(ObjectProvider<TaskDecorator> decorators) {
        return () -> {
            if (decorators.stream().count() > 1 && decorators.getIfUnique() == null) {
                throw new IllegalStateException("Multiple TaskDecorator beans without a @Primary one are found: "
                        + "Spring Boot silently ignores all of them in this case. "
                        + "Mark one as @Primary or leave a single TaskDecorator bean");
            }
        };
    }

    static class OnNoTaskDecoratorComposition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return !TaskDecoratorComposition.supported();
        }
    }
}
