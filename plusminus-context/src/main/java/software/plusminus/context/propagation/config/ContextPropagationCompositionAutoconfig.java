package software.plusminus.context.propagation.config;

import org.springframework.beans.factory.ObjectProvider;
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
@Conditional(ContextPropagationCompositionAutoconfig.OnTaskDecoratorComposition.class)
public class ContextPropagationCompositionAutoconfig {

    @Bean
    public TaskDecorator contextPropagationTaskDecorator(ObjectProvider<Context<?>> contexts) {
        return new ContextPropagationTaskDecorator(contexts.stream().collect(Collectors.toList()));
    }

    static class OnTaskDecoratorComposition implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            return TaskDecoratorComposition.supported();
        }
    }
}
