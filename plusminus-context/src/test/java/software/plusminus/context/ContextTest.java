package software.plusminus.context;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ContextTest {

    @Test
    void get() {
        Context<Integer> integerContext = Context.of(() -> 42);
        Context<String> stringContext = Context.of(() -> integerContext.get().toString());

        String valueFromContext = stringContext.get();

        assertThat(valueFromContext).isEqualTo("42");
    }

    @Test
    void optional() {
        Context<String> nullContext = Context.of(() -> null);
        Optional<String> optionalString = nullContext.optional();
        assertThat(optionalString).isEmpty();
    }

    @Test
    void constant() {
        Context<String> constant = Context.constant("42");
        String value = constant.get();
        assertThat(value).isEqualTo("42");
    }
}