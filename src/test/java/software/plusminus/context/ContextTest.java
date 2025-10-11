package software.plusminus.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextTest {

    @BeforeEach
    void before() {
        Context.init();
    }

    @AfterEach
    void after() {
        Context.clear();
    }

    @Test
    void typeResolving() {
        Context<Integer> integerContext = Context.of(() -> 42);
        Context<String> stringContext = Context.of(() -> integerContext.get().toString());

        String valueFromContext = stringContext.get();
        String valueInThreadLocal = String.class.cast(Context.VALUES.get().get(stringContext));

        assertThat(valueFromContext).isEqualTo("42");
        assertThat(valueInThreadLocal).isEqualTo("42");
    }
}