package software.plusminus.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ContextIntegrationTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
class ContextIntegrationTest {

    @SpyBean
    private Context<Integer> integerContext;
    @SpyBean
    private Context<String> stringContext;

    @BeforeEach
    void before() {
        Context.init();
    }

    @AfterEach
    void after() {
        Context.clear();
    }

    @Test
    void get() {
        String result = stringContext.get();
        assertThat(result).isEqualTo("42");
    }

    @Test
    void contextPopulation() {
        Integer beforeInitInteger = (Integer) Context.VALUES.get().get(integerContext);
        String beforeInitString = (String) Context.VALUES.get().get(stringContext);
        stringContext.get();
        Integer savedInteger = (Integer) Context.VALUES.get().get(integerContext);
        String savedString = (String) Context.VALUES.get().get(stringContext);

        assertThat(beforeInitInteger).isNull();
        assertThat(beforeInitString).isNull();
        assertThat(savedInteger).isEqualTo(42);
        assertThat(savedString).isEqualTo("42");
    }

    @Test
    void calls() {
        stringContext.get();
        stringContext.get();
        stringContext.get();

        verify(stringContext, times(3)).get();
        verify(stringContext).provide();
        verify(integerContext).get();
        verify(integerContext).provide();
    }

    @Configuration
    static class TestConfig {
        @Bean
        public Context<String> stringContext(Context<Integer> integerContext) {
            return Context.of(() -> integerContext.get().toString());
        }

        @Bean
        public Context<Integer> integerContext() {
            return Context.of(() -> 42);
        }
    }
}