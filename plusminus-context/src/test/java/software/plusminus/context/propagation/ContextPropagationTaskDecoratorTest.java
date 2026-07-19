package software.plusminus.context.propagation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import software.plusminus.context.Context;
import software.plusminus.context.WritableContext;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPropagationTaskDecoratorTest {

    private WritableContext<String> writableContext = WritableContext.of();
    private Context<String> threadNameContext = Context.of(() -> Thread.currentThread().getName());
    private Context<String> constantContext = Context.constant("constant");
    private ContextPropagationTaskDecorator decorator = new ContextPropagationTaskDecorator(
            Arrays.asList(writableContext, threadNameContext, constantContext));
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void propagatesWritableContext() throws Exception {
        writableContext.set("value");
        AtomicReference<String> seenByWorker = new AtomicReference<>();

        runDecorated(() -> seenByWorker.set(writableContext.get()));

        assertThat(seenByWorker.get()).isEqualTo("value");
    }

    @Test
    void propagatesReadOnlyThreadLocalContext() throws Exception {
        AtomicReference<String> seenByWorker = new AtomicReference<>();

        runDecorated(() -> seenByWorker.set(threadNameContext.get()));

        assertThat(seenByWorker.get()).isEqualTo(Thread.currentThread().getName());
    }

    @Test
    void ignoresConstantContext() throws Exception {
        AtomicReference<String> seenByWorker = new AtomicReference<>();

        runDecorated(() -> seenByWorker.set(constantContext.get()));

        assertThat(seenByWorker.get()).isEqualTo("constant");
    }

    @Test
    void clearsWorkerThreadAfterTask() throws Exception {
        writableContext.set("value");

        runDecorated(() -> { });

        assertThat(executor.submit(writableContext::get).get()).isNull();
    }

    @Test
    void doesNotChangeCallerContext() throws Exception {
        writableContext.set("value");

        runDecorated(() -> writableContext.replace("changed by worker"));

        assertThat(writableContext.get()).isEqualTo("value");
    }

    private void runDecorated(Runnable task) throws Exception {
        executor.submit(decorator.decorate(task)).get();
    }
}
