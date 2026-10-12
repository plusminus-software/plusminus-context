package software.plusminus.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnapshotTest {

    private Context<String> context = Context.of();
    private Context<String> thread = Context.of(() -> Thread.currentThread().getName());
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    @AfterEach
    void tearDown() {
        executor.shutdown();
    }

    @Test
    void copiesBoundValueToWorker() {
        Callable<String> read = context::get;

        String seenByWorker = Context.where(context, "value")
                .call(() -> submit(Context.snapshot().wrap(read)));

        assertThat(seenByWorker).isEqualTo("value");
    }

    @Test
    void doesNotCopySavedValueByDefault() {
        Callable<String> read = thread::get;

        String seenByWorker = Context.call(() -> {
            thread.get();
            return submit(Context.snapshot().wrap(read));
        });

        assertThat(seenByWorker).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    void copiesSavedValueOnDemand() {
        Callable<String> read = thread::get;

        String seenByWorker = Context.call(() -> {
            thread.get();
            return submit(Context.snapshot(true).wrap(read));
        });

        assertThat(seenByWorker).isEqualTo(Thread.currentThread().getName());
    }

    @Test
    void closesScopeOfWorkerAfterTask() throws Exception {
        Runnable nothing = () -> { };
        Runnable task = Context.where(context, "value")
                .call(() -> Context.snapshot().wrap(nothing));
        Callable<String> read = context::get;

        executor.submit(task).get();

        ExecutionException exception = assertThrows(ExecutionException.class,
                () -> executor.submit(read).get());
        assertThat(exception.getCause().getMessage()).isEqualTo("Scope is not initialized");
    }

    @Test
    void snapshotOutsideScopeOpensEmptyScope() {
        Callable<String> read = thread::get;

        String seenByWorker = submit(Context.snapshot().wrap(read));

        assertThat(seenByWorker).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    void taskInCallerThreadSeesCallerValues() {
        Runnable sees = () -> assertThat(context.get()).isEqualTo("value");
        Runnable task = Context.snapshot().wrap(sees);

        Context.where(context, "value").run(() -> {
            task.run();
            assertThat(context.get()).isEqualTo("value");
        });
    }

    @Test
    void workerDoesNotSeeLaterBindings() {
        Callable<String> read = context::get;
        Callable<String> task = Context.snapshot().wrap(read);
        ContextBindings bindings = Context.where(context, "value");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> bindings.call(() -> submit(task)));

        assertThat(exception.getCause().getCause().getMessage()).isEqualTo("Context is not bound");
    }

    private <V> V submit(Callable<V> task) {
        try {
            return executor.submit(task).get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
