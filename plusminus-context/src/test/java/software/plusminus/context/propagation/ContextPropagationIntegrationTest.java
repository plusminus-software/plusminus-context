package software.plusminus.context.propagation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.plusminus.context.Context;
import software.plusminus.context.propagation.config.ContextPropagationAutoconfig;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPropagationIntegrationTest {

    private Context<String> testContext = Context.required();
    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ContextPropagationAutoconfig.class));

    @Test
    void propagatesContextThroughTaskExecutor() {
        runner.run(context -> assertThat(seenByWorker(context.getBean(TaskDecorator.class)))
                .isEqualTo("value"));
    }

    @Test
    void wrapsCustomTaskDecoratorWithPropagation() {
        runner.withUserConfiguration(CustomDecoratorConfig.class).run(context -> {
            assertThat(context.getBeansOfType(TaskDecorator.class))
                    .hasSize(1);
            assertThat(seenByWorker(context.getBean(TaskDecorator.class)))
                    .isEqualTo("value");
        });
    }

    private String seenByWorker(TaskDecorator decorator) throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(decorator);
        executor.initialize();
        try {
            return Context.where(testContext, "value")
                    .call(() -> executor.submit(testContext::get))
                    .get();
        } finally {
            executor.shutdown();
        }
    }

    @Configuration
    static class CustomDecoratorConfig {
        @Bean
        TaskDecorator customTaskDecorator() {
            return runnable -> runnable;
        }
    }
}
