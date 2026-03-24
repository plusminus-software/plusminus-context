package software.plusminus.context;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WritableContextTest {

    @Test
    void writableContextInRegularContext() {
        WritableContext<Integer> integerContext = WritableContext.of(42);
        Context<String> stringContext = Context.of(() -> integerContext.get().toString());

        String valueFromContext = stringContext.get();

        assertThat(valueFromContext).isEqualTo("42");
    }

    @Test
    void set() {
        WritableContext<Integer> integerContext = WritableContext.of();
        integerContext.set(42);

        Integer currentValue = integerContext.get();

        assertThat(currentValue).isEqualTo(42);
    }

    @Test
    void setIfAlreadyPresent() {
        WritableContext<Integer> integerContext = WritableContext.of();
        integerContext.set(42);
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> integerContext.set(43));

        assertThat(exception.getMessage()).isEqualTo("Context has already been set");
    }

    @Test
    void optional() {
        WritableContext<Integer> integerContext = WritableContext.of();

        Optional<Integer> empty = integerContext.optional();
        integerContext.set(42);
        Optional<Integer> present = integerContext.optional();

        assertThat(empty).isEmpty();
        assertThat(present).isPresent().contains(42);
    }
}
