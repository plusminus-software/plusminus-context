package software.plusminus.context;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextTest {

    private Context<String> context = Context.of();

    @Test
    void getFailsOutsideScope() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, context::get);

        assertThat(exception.getMessage()).isEqualTo("Scope is not initialized");
    }

    @Test
    void getFailsWhenNotBound() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> Context.run(context::get));

        assertThat(exception.getMessage()).isEqualTo("Context is not bound");
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
    void nullFromSupplierIsNotSaved() {
        AtomicInteger calls = new AtomicInteger();
        Context<String> nullContext = Context.of(() -> {
            calls.incrementAndGet();
            return null;
        });

        Context.run(() -> {
            assertThat(nullContext.get()).isNull();
            assertThat(nullContext.get()).isNull();
        });
        assertThat(calls.get()).isEqualTo(2);
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
