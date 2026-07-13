package software.plusminus.scope;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThrowingRunnableTest {

    @Test
    void asRunnableRunsWithoutException() {
        boolean[] called = {false};
        ThrowingRunnable throwing = () -> called[0] = true;
        throwing.asRunnable().run();
        assertThat(called[0]).isTrue();
    }

    @Test
    void asRunnableSneakyThrowsCheckedException() {
        ThrowingRunnable throwing = () -> {
            throw new Exception("checked");
        };
        Exception e = assertThrows(Exception.class, () -> throwing.asRunnable().run());
        assertThat(e.getMessage()).isEqualTo("checked");
    }

    @Test
    void ofSneakyThrowsRuntimeException() {
        ThrowingRunnable throwing = ThrowingRunnable.of(() -> {
            throw new IllegalStateException("boom");
        });
        IllegalStateException e = assertThrows(IllegalStateException.class, throwing::run);
        assertThat(e.getMessage()).isEqualTo("boom");
    }
}
