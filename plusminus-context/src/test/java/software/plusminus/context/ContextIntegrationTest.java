package software.plusminus.context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ContextIntegrationTest.TestConfig.class)
class ContextIntegrationTest {

    @Autowired
    private Context<Integer> integerContext;
    @Autowired
    private Context<String> stringContext;

    @Test
    void get() {
        assertThat(stringContext.optional()).isEmpty();
        Context.where(integerContext, () -> 42).run(() -> assertThat(stringContext.get()).isEqualTo("42"));
    }

    @Configuration
    static class TestConfig {
        @Bean
        public Context<String> stringContext(Context<Integer> integerContext) {
            return integerContext.map(Object::toString);
        }

        @Bean
        public Context<Integer> integerContext() {
            return Context.of();
        }
    }
}
