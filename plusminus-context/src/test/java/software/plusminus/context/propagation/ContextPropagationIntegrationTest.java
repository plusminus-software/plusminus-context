package software.plusminus.context.propagation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.plusminus.context.WritableContext;
import software.plusminus.context.propagation.config.ContextPropagationAutoconfig;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {ContextPropagationAutoconfig.class, ContextPropagationIntegrationTest.TestConfig.class})
class ContextPropagationIntegrationTest {

    @Autowired
    private TaskDecorator decorator;
    @Autowired
    private WritableContext<String> testContext;

    @Test
    void propagatesContextThroughTaskExecutor() throws Exception {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(decorator);
        executor.initialize();
        testContext.set("value");
        AtomicReference<String> seenByWorker = new AtomicReference<>();

        executor.submit(() -> seenByWorker.set(testContext.get())).get();

        assertThat(seenByWorker.get()).isEqualTo("value");
        executor.shutdown();
    }

    @Test
    void failsOnDuplicateTaskDecorator() {
        new ApplicationContextRunner()
                .withUserConfiguration(ContextPropagationAutoconfig.class, CustomDecoratorConfig.class)
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration
    static class TestConfig {
        @Bean
        WritableContext<String> testContext() {
            return WritableContext.of();
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
