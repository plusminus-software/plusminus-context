package software.plusminus.context.propagation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
    void wrapsCustomTaskDecoratorWithPropagation() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ContextPropagationAutoconfig.class))
                .withUserConfiguration(TestConfig.class, CustomDecoratorConfig.class)
                .run(context -> {
                    WritableContext<String> customContext = context.getBean(WritableContext.class);
                    customContext.set("value");
                    AtomicReference<String> seenByWorker = new AtomicReference<>();

                    Thread thread = new Thread(context.getBean(TaskDecorator.class)
                            .decorate(() -> seenByWorker.set(customContext.get())));
                    thread.start();
                    thread.join();

                    assertThat(seenByWorker.get()).isEqualTo("value");
                });
    }

    @Test
    void failsOnMultipleTaskDecoratorsWithoutPrimary() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ContextPropagationAutoconfig.class))
                .withUserConfiguration(CustomDecoratorConfig.class, SecondDecoratorConfig.class)
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void startsOnMultipleTaskDecoratorsWithPrimary() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ContextPropagationAutoconfig.class))
                .withUserConfiguration(CustomDecoratorConfig.class, PrimaryDecoratorConfig.class)
                .run(context -> assertThat(context).hasNotFailed());
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

    @Configuration
    static class SecondDecoratorConfig {
        @Bean
        TaskDecorator secondTaskDecorator() {
            return runnable -> runnable;
        }
    }

    @Configuration
    static class PrimaryDecoratorConfig {
        @Bean
        @Primary
        TaskDecorator primaryTaskDecorator() {
            return runnable -> runnable;
        }
    }
}
