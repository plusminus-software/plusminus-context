package software.plusminus.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SnapshotTest {

    private Context<String> context = Context.required();
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
    void doesNotCopyCachedValue() {
        Callable<String> read = thread::get;

        String seenByWorker = Context.call(() -> {
            thread.get();
            return submit(Context.snapshot().wrap(read));
        });

        assertThat(seenByWorker).isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    void copiesValueBoundWithoutValue() {
        Callable<String> read = thread::get;

        String seenByWorker = Context.where(thread)
                .call(() -> submit(Context.snapshot().wrap(read)));

        assertThat(seenByWorker).isEqualTo(Thread.currentThread().getName());
    }

    @Test
    void closesScopeOfWorkerAfterTask() throws Exception {
        Runnable nothing = () -> { };
        Runnable task = Context.where(context, "value")
                .call(() -> Context.snapshot().wrap(nothing));
        Callable<Boolean> inScope = () -> Scope.current() != null;

        executor.submit(task).get();

        assertThat(executor.submit(inScope).get()).isFalse();
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
        ScopeBuilder builder = Context.where(context, "value");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> builder.call(() -> submit(task)));

        assertThat(exception.getCause())
                .isInstanceOf(ExecutionException.class);
        assertThat(exception.getCause().getCause())
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No value in context");
    }

    private <V> V submit(Callable<V> task) {
        try {
            return executor.submit(task).get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
