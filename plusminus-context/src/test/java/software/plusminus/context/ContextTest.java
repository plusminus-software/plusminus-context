package software.plusminus.context;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextTest {

    private Context<String> context = Context.of();

    @Test
    void getFailsOutsideScope() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, context::get);

        assertThat(exception.getMessage()).isEqualTo("No active scope");
    }

    @Test
    void getFailsWhenNotBound() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> Context.run(context::get));

        assertThat(exception.getMessage()).isEqualTo("No value in context");
    }

    @Test
    void runBindsValue() {
        Context.where(context, "value").run(() -> assertThat(context.get()).isEqualTo("value"));
    }

    @Test
    void runRestoresPreviousValue() {
        Context.where(context, "outer").run(() -> {
            Context.where(context, "inner").run(() -> assertThat(context.get()).isEqualTo("inner"));
            assertThat(context.get()).isEqualTo("outer");
        });

        assertThrows(IllegalStateException.class, context::get);
    }

    @Test
    void runRestoresPreviousValueOnException() {
        ContextBindings bindings = Context.where(context, "value");

        assertThrows(UnsupportedOperationException.class, () -> bindings.run(() -> {
            throw new UnsupportedOperationException();
        }));

        assertThrows(IllegalStateException.class, context::get);
    }

    @Test
    void innerRunKeepsOuterBindings() {
        Context<Integer> second = Context.of();

        Context.where(context, "value").run(() ->
                Context.where(second, 42).run(() -> {
                    assertThat(context.get()).isEqualTo("value");
                    assertThat(second.get()).isEqualTo(42);
                }));
    }

    @Test
    void whereBindsSeveralContexts() {
        Context<Integer> second = Context.of();

        String result = Context.where(context, "value")
                .where(second, 42)
                .call(() -> context.get() + second.get());

        assertThat(result).isEqualTo("value42");
    }

    @Test
    void whereReplacesOwnValue() {
        Context<String> constant = Context.of("value");

        Context.where(constant, "other").run(() -> assertThat(constant.get()).isEqualTo("other"));

        assertThat(Context.call(constant::get)).isEqualTo("value");
    }

    @Test
    void constantWorksOutsideScope() {
        Context<String> constant = Context.of("value");

        assertThat(constant.get()).isEqualTo("value");
    }

    @Test
    void whereFailsOnNullValue() {
        assertThrows(NullPointerException.class, () -> Context.where(context, null));
    }

    @Test
    void supplierIsCalledOncePerScope() {
        AtomicInteger calls = new AtomicInteger();
        Context<Integer> counter = Context.of(calls::incrementAndGet);

        Context.run(() -> {
            assertThat(counter.get()).isEqualTo(1);
            assertThat(counter.get()).isEqualTo(1);
            Context.run(() -> assertThat(counter.get()).isEqualTo(2));
            assertThat(counter.get()).isEqualTo(1);
        });
    }

    @Test
    void innerScopeDoesNotSaveValueInOuterScope() {
        AtomicInteger calls = new AtomicInteger();
        Context<Integer> counter = Context.of(calls::incrementAndGet);

        Context.run(() -> {
            Context.run(() -> assertThat(counter.get()).isEqualTo(1));
            assertThat(counter.get()).isEqualTo(2);
        });
    }

    @Test
    void supplierReadsOtherContext() {
        Context<Integer> length = Context.of(() -> context.get().length());

        Context.where(context, "value").run(() -> assertThat(length.get()).isEqualTo(5));
    }

    @Test
    void getFailsWhenSupplierReturnsNull() {
        Context<String> nullContext = Context.of(() -> null);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> Context.run(nullContext::get));

        assertThat(exception.getMessage()).isEqualTo("No value in context");
    }

    @Test
    void mapIsCalledOncePerScope() {
        AtomicInteger calls = new AtomicInteger();
        Context<String> mapped = context.map(value -> value + calls.incrementAndGet());

        Context.where(context, "value").run(() -> {
            assertThat(mapped.get()).isEqualTo("value1");
            assertThat(mapped.get()).isEqualTo("value1");
            Context.where(context, "other").run(() -> assertThat(mapped.get()).isEqualTo("other2"));
            Context.where(mapped, "bound").run(() -> assertThat(mapped.get()).isEqualTo("bound"));
        });
    }
}
