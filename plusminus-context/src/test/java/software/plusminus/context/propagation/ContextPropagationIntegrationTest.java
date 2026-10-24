package software.plusminus.context.propagation;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import software.plusminus.context.Context;
import software.plusminus.context.propagation.config.ContextPropagationAutoconfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPropagationIntegrationTest {

    private static final Context<String> TEST_CONTEXT = Context.required();

    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    ContextPropagationAutoconfig.class, TaskExecutionAutoConfiguration.class));

    @Test
    void propagatesContextThroughExecute() {
        runner.run(context -> assertThat(seenByExecutor(context)).containsExactly("task:value"));
    }

    @Test
    void appliesUserDecoratorsInsidePropagatedContext() {
        runner.withUserConfiguration(UserDecoratorsConfig.class).run(context -> {
            assertThat(context).hasSingleBean(FirstDecorator.class);
            assertThat(context).hasSingleBean(SecondDecorator.class);
            assertThat(seenByExecutor(context))
                    .containsExactly("first:value", "second:value", "task:value");
        });
    }

    @Test
    void propagatesContextToAsyncMethod() {
        runner.withUserConfiguration(AsyncConfig.class).run(context -> {
            AsyncService service = context.getBean(AsyncService.class);

            Context.where(TEST_CONTEXT, "value").run(service::read);

            assertThat(service.latch().await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(service.seen()).containsExactly("value");
        });
    }

    @Test
    void canBeDisabled() {
        runner.withPropertyValues("plusminus.context.propagation.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(ContextPropagationTaskDecorator.class));
    }

    private static List<String> seenByExecutor(ApplicationContext context) throws InterruptedException {
        ThreadPoolTaskExecutor executor = context.getBean("applicationTaskExecutor", ThreadPoolTaskExecutor.class);
        List<String> events = context.getBeanProvider(Events.class).getIfAvailable(Events::new).list;
        CountDownLatch latch = new CountDownLatch(1);

        Context.where(TEST_CONTEXT, "value").run(() -> executor.execute(() -> {
            events.add("task:" + read());
            latch.countDown();
        }));

        assertThat(latch.await(1, TimeUnit.SECONDS)).isTrue();
        return events;
    }

    private static String read() {
        return TEST_CONTEXT.optional().orElse("none");
    }

    static class Events {
        private final List<String> list = new CopyOnWriteArrayList<>();
    }

    static class RecordingDecorator implements TaskDecorator, Ordered {

        private final String name;
        private final int order;
        private final Events events;

        RecordingDecorator(String name, int order, Events events) {
            this.name = name;
            this.order = order;
            this.events = events;
        }

        @Override
        public Runnable decorate(Runnable task) {
            return () -> {
                events.list.add(name + ":" + read());
                task.run();
            };
        }

        @Override
        public int getOrder() {
            return order;
        }
    }

    static class FirstDecorator extends RecordingDecorator {
        FirstDecorator(Events events) {
            super("first", 1, events);
        }
    }

    static class SecondDecorator extends RecordingDecorator {
        SecondDecorator(Events events) {
            super("second", 2, events);
        }
    }

    @Configuration
    static class UserDecoratorsConfig {

        @Bean
        Events events() {
            return new Events();
        }

        @Bean
        SecondDecorator secondDecorator(Events events) {
            return new SecondDecorator(events);
        }

        @Bean
        FirstDecorator firstDecorator(Events events) {
            return new FirstDecorator(events);
        }
    }

    static class AsyncService {

        private final List<String> seen = new CopyOnWriteArrayList<>();
        private final CountDownLatch latch = new CountDownLatch(1);

        @Async
        public void read() {
            seen.add(ContextPropagationIntegrationTest.read());
            latch.countDown();
        }

        public List<String> seen() {
            return seen;
        }

        public CountDownLatch latch() {
            return latch;
        }
    }

    @Configuration
    @EnableAsync
    static class AsyncConfig {

        @Bean
        AsyncService asyncService() {
            return new AsyncService();
        }
    }
}
