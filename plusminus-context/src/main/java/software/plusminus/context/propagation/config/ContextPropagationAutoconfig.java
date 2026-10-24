package software.plusminus.context.propagation.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskDecorator;
import software.plusminus.context.propagation.ContextPropagationTaskDecorator;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(name = "plusminus.context.propagation.enabled", matchIfMissing = true)
public class ContextPropagationAutoconfig {

    @Bean
    @Primary
    public ContextPropagationTaskDecorator contextPropagationTaskDecorator(ObjectProvider<TaskDecorator> decorators) {
        return new ContextPropagationTaskDecorator(new OtherDecorators(decorators));
    }

    private static final class OtherDecorators implements TaskDecorator {

        private final ObjectProvider<TaskDecorator> decorators;
        private volatile List<TaskDecorator> resolved;

        private OtherDecorators(ObjectProvider<TaskDecorator> decorators) {
            this.decorators = decorators;
        }

        @Override
        public Runnable decorate(Runnable task) {
            List<TaskDecorator> list = resolve();
            Runnable result = task;
            for (int i = list.size() - 1; i >= 0; i--) {
                result = list.get(i).decorate(result);
            }
            return result;
        }

        private List<TaskDecorator> resolve() {
            List<TaskDecorator> result = resolved;
            if (result == null) {
                result = decorators.orderedStream()
                        .filter(decorator -> !(decorator instanceof ContextPropagationTaskDecorator))
                        .collect(Collectors.toList());
                resolved = result;
            }
            return result;
        }
    }
}
