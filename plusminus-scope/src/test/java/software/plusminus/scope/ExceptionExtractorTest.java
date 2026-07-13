package software.plusminus.scope;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionExtractorTest {

    @Test
    void returnsSameExceptionWhenNothingToExtract() {
        Exception e = new IllegalStateException();
        assertThat(ExceptionExtractor.extract(e)).isSameAs(e);
    }

    @Test
    void returnsSameExceptionWhenTypeNotListed() {
        Exception e = new IllegalStateException(new IllegalArgumentException());
        assertThat(ExceptionExtractor.extract(e, RuntimeException.class)).isSameAs(e);
    }

    @Test
    void extractsCauseWhenTypeMatches() {
        Exception cause = new IllegalArgumentException();
        Exception e = new IllegalStateException(cause);
        assertThat(ExceptionExtractor.extract(e, IllegalStateException.class)).isSameAs(cause);
    }

    @Test
    void extractsRecursively() {
        Exception root = new IllegalArgumentException();
        Exception middle = new IllegalStateException(root);
        Exception outer = new IllegalStateException(middle);
        assertThat(ExceptionExtractor.extract(outer, IllegalStateException.class)).isSameAs(root);
    }

    @Test
    void returnsSameExceptionWhenCauseIsMissing() {
        Exception e = new IllegalStateException();
        assertThat(ExceptionExtractor.extract(e, IllegalStateException.class)).isSameAs(e);
    }

    @Test
    void returnsSameExceptionWhenCauseIsNotException() {
        Exception e = new IllegalStateException(new StackOverflowError());
        assertThat(ExceptionExtractor.extract(e, IllegalStateException.class)).isSameAs(e);
    }
}
