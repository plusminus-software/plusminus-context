package software.plusminus.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WritableContextTest {

    @BeforeEach
    void before() {
        Context.init();
    }

    @AfterEach
    void after() {
        Context.clear();
    }

    @Test
    void writableContextInRegularContext() {
        WritableContext<Integer> integerContext = WritableContext.of(42);
        Context<String> stringContext = Context.of(() -> integerContext.get().toString());

        String valueFromContext = stringContext.get();
        String valueInThreadLocal = String.class.cast(Context.VALUES.get().get(stringContext));

        assertThat(valueFromContext).isEqualTo("42");
        assertThat(valueInThreadLocal).isEqualTo("42");
    }

    @Test
    void set() {
        WritableContext<Integer> integerContext = WritableContext.of();
        integerContext.set(42);

        Integer currentValue = integerContext.get();
        Integer valueInThreadLocal = Integer.class.cast(Context.VALUES.get().get(integerContext));

        assertThat(currentValue).isEqualTo(42);
        assertThat(valueInThreadLocal).isEqualTo(42);
    }

    @Test
    void setIfAltreadyPresent() {
        WritableContext<Integer> integerContext = WritableContext.of();
        integerContext.set(42);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> integerContext.set(43));

        assertThat(exception.getMessage()).isEqualTo("Cannot update context to 43 as the context already contains 42");
    }

    @Test
    void getIfPresent() {
        WritableContext<Integer> integerContext = WritableContext.of();

        Optional<Integer> empty = integerContext.getIfPresent();
        integerContext.set(42);
        Optional<Integer> present = integerContext.getIfPresent();

        assertThat(empty).isEmpty();
        assertThat(present).isPresent()
                .contains(42);
    }
}
