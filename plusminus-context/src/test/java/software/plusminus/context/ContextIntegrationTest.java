package software.plusminus.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = ContextIntegrationTest.TestConfig.class)
@ExtendWith(MockitoExtension.class)
class ContextIntegrationTest {

    @SpyBean
    private Context<Integer> integerContext;
    @Autowired
    private Context<String> stringContext;
    @Autowired
    private List<ClearableContext<?>> clearableContexts;

    @AfterEach
    void after() {
        clearableContexts.forEach(ClearableContext::clear);
    }

    @Test
    void get() {
        String result = stringContext.get();
        assertThat(result).isEqualTo("42");
        verify(integerContext).get();
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