package software.plusminus.context.propagation.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContextPropagationAutoconfigTest {

    @Test
    void taskDecoratorCompositionSupportedSinceSpringBoot4() {
        assertThat(TaskDecoratorComposition.supported(null)).isFalse();
        assertThat(TaskDecoratorComposition.supported("unknown")).isFalse();
        assertThat(TaskDecoratorComposition.supported("2.2.2.RELEASE")).isFalse();
        assertThat(TaskDecoratorComposition.supported("3.5.4")).isFalse();
        assertThat(TaskDecoratorComposition.supported("4.0.1")).isTrue();
        assertThat(TaskDecoratorComposition.supported("5.0.0")).isTrue();
    }
}
