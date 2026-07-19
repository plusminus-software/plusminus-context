package software.plusminus.context.propagation.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import software.plusminus.context.Context;
import software.plusminus.context.propagation.ContextPropagationTaskDecorator;

import java.util.stream.Collectors;

@Configuration
public class ContextPropagationAutoconfig {

    @Bean
    public TaskDecorator contextPropagationTaskDecorator(ObjectProvider<Context<?>> contexts) {
        return new ContextPropagationTaskDecorator(contexts.stream().collect(Collectors.toList()));
    }

    @Bean
    public SmartInitializingSingleton taskDecoratorDuplicationCheck(ObjectProvider<TaskDecorator> decorators) {
        return () -> {
            long count = decorators.stream().count();
            if (count > 1) {
                throw new IllegalStateException("Found " + count + " TaskDecorator beans: "
                        + "Spring Boot silently ignores all of them if more than one is present. "
                        + "Remove the custom TaskDecorator or compose it with ContextPropagationTaskDecorator");
            }
        };
    }
}
