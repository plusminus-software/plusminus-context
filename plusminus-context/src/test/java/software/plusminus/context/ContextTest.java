package software.plusminus.context;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextTest {

    private Context<String> context = Context.of();

    @Test
    void getFailsWhenNotBound() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, context::get);

        assertThat(exception.getMessage()).isEqualTo("Context is not bound");
    }

    @Test
    void optionalIsEmptyWhenNotBound() {
        assertThat(context.optional()).isEmpty();
    }

    @Test
    void runBindsValue() {
        Context.where(context, () -> "value").run(() -> {
            assertThat(context.get()).isEqualTo("value");
            assertThat(context.optional()).contains("value");
        });
    }

    @Test
    void runRestoresPreviousValue() {
        Context.where(context, () -> "outer").run(() -> {
            Context.where(context, () -> "inner").run(() -> assertThat(context.get()).isEqualTo("inner"));
            assertThat(context.get()).isEqualTo("outer");
        });

        assertThat(context.optional()).isEmpty();
    }

    @Test
    void runRestoresPreviousValueOnException() {
        ContextBindings bindings = Context.where(context, () -> "value");

        assertThrows(IllegalStateException.class, () -> bindings.run(() -> {
            throw new IllegalStateException();
        }));

        assertThat(context.optional()).isEmpty();
    }

    @Test
    void innerRunKeepsOuterBindings() {
        Context<Integer> second = Context.of();

        Context.where(context, () -> "value").run(() ->
                Context.where(second, () -> 42).run(() -> {
                    assertThat(context.get()).isEqualTo("value");
                    assertThat(second.get()).isEqualTo(42);
                }));
    }

    @Test
    void whereBindsSeveralContexts() {
        Context<Integer> second = Context.local();

        String result = Context.where(context, () -> "value")
                .where(second, () -> 42)
                .call(() -> context.get() + second.get());

        assertThat(result).isEqualTo("value42");
    }

    @Test
    void callThrowsCheckedException() {
        ContextBindings bindings = Context.where(context, () -> "value");

        assertThrows(IOException.class, () -> bindings.call(() -> {
            throw new IOException();
        }));
    }

    @Test
    void whereFailsOnContextThatCannotBeBound() {
        Context<String> constant = Context.constant("value");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> Context.where(constant, () -> "other"));

        assertThat(exception.getMessage()).isEqualTo("Context cannot be bound");
    }

    @Test
    void supplierIsCalledOncePerRun() {
        AtomicInteger calls = new AtomicInteger();
        ContextBindings bindings = Context.where(context, () -> "value" + calls.incrementAndGet());

        bindings.run(() -> { });
        assertThat(calls.get()).isZero();
        bindings.run(() -> {
            assertThat(context.get()).isEqualTo("value1");
            assertThat(context.get()).isEqualTo("value1");
        });
        bindings.run(() -> assertThat(context.get()).isEqualTo("value2"));
    }

    @Test
    void whereAfterWhere() {
        Context<Integer> second = Context.of();

        Context.where(context, () -> "value")
                .where(second, () -> context.get().length())
                .run(() -> assertThat(second.get()).isEqualTo(5));
    }

    @Test
    void supplierMayReturnNull() {
        Context.where(context, () -> null).run(() -> assertThat(context.optional()).isEmpty());
    }

    @Test
    void constant() {
        assertThat(Context.constant("42").get()).isEqualTo("42");
    }

    @Test
    void supplier() {
        Context<Integer> integerContext = Context.of(() -> 42);
        Context<String> nullContext = Context.of(() -> null);

        assertThat(integerContext.get()).isEqualTo(42);
        assertThat(nullContext.optional()).isEmpty();
    }

    @Test
    void map() {
        Context<Integer> length = context.map(String::length);

        assertThat(length.optional()).isEmpty();
        Context.where(context, () -> "value").run(() -> assertThat(length.get()).isEqualTo(5));
    }
}
