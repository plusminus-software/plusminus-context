package software.plusminus.context.propagation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.plusminus.context.Context;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPropagationTaskDecoratorTest {

    private Context<String> context = Context.required();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private AtomicReference<String> seenByWorker = new AtomicReference<>();

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void propagatesContext() {
        ContextPropagationTaskDecorator decorator = new ContextPropagationTaskDecorator();

        Runnable task = () -> seenByWorker.set(context.get());

        Context.where(context, "value")
                .run(() -> submit(decorator.decorate(task)));

        assertThat(seenByWorker.get()).isEqualTo("value");
    }

    @Test
    void composesDelegateDecorator() {
        ContextPropagationTaskDecorator decorator = new ContextPropagationTaskDecorator(task -> () -> {
            seenByWorker.set("delegate:" + context.get());
            task.run();
        });

        Context.where(context, "value")
                .run(() -> submit(decorator.decorate(() -> { })));

        assertThat(seenByWorker.get()).isEqualTo("delegate:value");
    }

    private void submit(Runnable task) {
        try {
            executor.submit(task).get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
