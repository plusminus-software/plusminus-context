package software.plusminus.context;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContextTest {

    private Context<String> context = Context.required();

    @Test
    void requiredFailsOutsideScope() {
        IllegalStateException exception = assertThrows(IllegalStateException.class, context::get);

        assertThat(exception.getMessage()).isEqualTo("No active scope");
    }

    @Test
    void requiredFailsWhenNotBound() {
        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> Context.run(context::get));

        assertThat(exception.getMessage()).isEqualTo("No value in context");
    }

    @Test
    void optionalOfRequiredIsEmptyOutsideScope() {
        assertThat(context.optional()).isEmpty();
    }

    @Test
    void optionalOfRequiredIsEmptyWhenNotBound() {
        Context.run(() -> assertThat(context.optional()).isEmpty());
    }

    @Test
    void optionalOfRequiredHasBoundValue() {
        Context.where(context, "value").run(() -> assertThat(context.optional()).contains("value"));
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
        ScopeBuilder builder = Context.where(context, "value");

        assertThrows(UnsupportedOperationException.class, () -> builder.run(() -> {
            throw new UnsupportedOperationException();
        }));

        assertThrows(IllegalStateException.class, context::get);
    }

    @Test
    void innerRunKeepsOuterBindings() {
        Context<Integer> second = Context.required();

        Context.where(context, "value").run(() ->
                Context.where(second, 42).run(() -> {
                    assertThat(context.get()).isEqualTo("value");
                    assertThat(second.get()).isEqualTo(42);
                }));
    }

    @Test
    void whereBindsSeveralContexts() {
        Context<Integer> second = Context.required();

        String result = Context.where(context, "value")
                .where(second, 42)
                .call(() -> context.get() + second.get());

        assertThat(result).isEqualTo("value42");
    }

    @Test
    void whereBindsNull() {
        Context<String> constant = Context.constant("value");

        Context.where(constant, null).run(() -> {
            assertThat(constant.get()).isNull();
            assertThat(constant.optional()).isEmpty();
        });
    }

    @Test
    void whereWithoutValueBindsCurrentValue() {
        AtomicInteger calls = new AtomicInteger();
        Context<Integer> counter = Context.of(calls::incrementAndGet);

        Context.where(counter).run(() -> {
            assertThat(counter.get()).isEqualTo(1);
            Context.run(() -> assertThat(counter.get()).isEqualTo(1));
        });
    }

    @Test
    void whereWithoutValueKeepsOuterValue() {
        AtomicInteger calls = new AtomicInteger();
        Context<Integer> counter = Context.of(calls::incrementAndGet);

        Context.where(counter).run(() ->
                Context.where(counter).run(() -> assertThat(counter.get()).isEqualTo(1)));
    }

    @Test
    void constantWorksOutsideScope() {
        Context<String> constant = Context.constant("value");

        assertThat(constant.get()).isEqualTo("value");
    }

    @Test
    void whereReplacesConstant() {
        Context<String> constant = Context.constant("value");

        Context.where(constant, "other").run(() -> assertThat(constant.get()).isEqualTo("other"));

        assertThat(Context.call(constant::get)).isEqualTo("value");
    }

    @Test
    void constantIsNotCached() {
        Context<String> constant = Context.constant("value");

        Context.run(() -> {
            constant.get();
            assertThat(Scope.current().contains(constant)).isFalse();
        });
    }

    @Test
    void whereReplacesValueOfLambda() {
        Context<String> lambda = () -> "value";

        Context.where(lambda, "other").run(() -> assertThat(lambda.get()).isEqualTo("other"));

        assertThat(lambda.get()).isEqualTo("value");
    }

    @Test
    void supplierIsCalledOnEveryReadOutsideScope() {
        AtomicInteger calls = new AtomicInteger();
        Context<Integer> counter = Context.of(calls::incrementAndGet);

        assertThat(counter.get()).isEqualTo(1);
        assertThat(counter.get()).isEqualTo(2);
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
    void innerScopeRecomputesValueWithRebindDependency() {
        Context<Integer> length = Context.of(() -> context.get().length());

        Context.where(context, "value").run(() -> {
            assertThat(length.get()).isEqualTo(5);
            Context.where(context, "ab").run(() -> assertThat(length.get()).isEqualTo(2));
        });
    }

    @Test
    void nullFromSupplierIsCached() {
        AtomicInteger calls = new AtomicInteger();
        Context<String> nullContext = Context.of(() -> {
            calls.incrementAndGet();
            return null;
        });

        Context.run(() -> {
            assertThat(nullContext.get()).isNull();
            assertThat(nullContext.get()).isNull();
        });

        assertThat(calls.get()).isEqualTo(1);
    }

    @Test
    void dynamicIsCalledOnEveryRead() {
        AtomicInteger calls = new AtomicInteger();
        Context<Integer> dynamic = Context.dynamic(calls::incrementAndGet);

        Context.run(() -> {
            assertThat(dynamic.get()).isEqualTo(1);
            assertThat(dynamic.get()).isEqualTo(2);
            Context.where(dynamic, 42).run(() -> assertThat(dynamic.get()).isEqualTo(42));
        });
    }

    @Test
    void mapIsCalledOnEveryRead() {
        AtomicInteger calls = new AtomicInteger();
        Context<String> mapped = context.map(value -> value + calls.incrementAndGet());

        Context.where(context, "value").run(() -> {
            assertThat(mapped.get()).isEqualTo("value1");
            assertThat(mapped.get()).isEqualTo("value2");
            Context.where(mapped, "bound")
                    .run(() -> assertThat(mapped.get()).isEqualTo("bound"));
        });
    }
}
