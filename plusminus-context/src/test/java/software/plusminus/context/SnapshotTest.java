package software.plusminus.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class SnapshotTest {

    private Context<String> context = Context.of();
    private Context<String> local = Context.local();
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void propagatesValueToWorker() throws Exception {
        Optional<String> seenByWorker = Context.where(context, () -> "value")
                .call(() -> executor.submit(Context.snapshot().wrap(context::optional)).get());

        assertThat(seenByWorker).contains("value");
    }

    @Test
    void sharesSuppliedValueWithWorker() throws Exception {
        AtomicInteger calls = new AtomicInteger();

        Context.where(context, () -> "value" + calls.incrementAndGet()).call(() -> {
            Optional<String> seenByWorker = executor.submit(Context.snapshot().wrap(context::optional)).get();
            assertThat(seenByWorker).contains("value1");
            assertThat(context.get()).isEqualTo("value1");
            return null;
        });
    }

    @Test
    void doesNotPropagateLocalValue() throws Exception {
        Optional<String> seenByWorker = Context.where(local, () -> "value")
                .call(() -> executor.submit(Context.snapshot().wrap(local::optional)).get());

        assertThat(seenByWorker).isEmpty();
    }

    @Test
    void restoresWorkerAfterTask() throws Exception {
        Runnable task = Context.where(context, () -> "value")
                .call(() -> Context.snapshot().wrap(() -> { }));

        executor.submit(task).get();

        assertThat(executor.submit(context::optional).get()).isEmpty();
    }

    @Test
    void keepsCallerValuesWhenTaskRunsInCallerThread() {
        Runnable seesNothing = () -> assertThat(context.optional()).isEmpty();
        Runnable task = Context.snapshot().wrap(seesNothing);

        Context.where(context, () -> "value").run(() -> {
            task.run();
            assertThat(context.get()).isEqualTo("value");
        });
    }

    @Test
    void doesNotSeeLaterBindings() throws Exception {
        Callable<Optional<String>> task = Context.snapshot().wrap(context::optional);

        Optional<String> seenByWorker = Context.where(context, () -> "value")
                .call(() -> executor.submit(task).get());

        assertThat(seenByWorker).isEmpty();
    }
}
