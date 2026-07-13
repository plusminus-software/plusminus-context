package software.plusminus.scope;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AroundScopeTest {

    @Test
    void returnsSameRunnableWhenNoArounds() {
        Runnable runnable = () -> { };
        assertThat(AroundScope.around(Collections.emptyList(), this, runnable)).isSameAs(runnable);
    }

    @Test
    void wrapsRunnableInOrder() {
        List<String> calls = new ArrayList<>();
        List<AroundScope> arounds = new ArrayList<>();
        arounds.add(around(calls, "first"));
        arounds.add(around(calls, "second"));

        AroundScope.run(arounds, this, () -> calls.add("scope"));

        assertThat(calls).containsExactly("first", "second", "scope");
    }

    @Test
    void skipsNotSupportingArounds() {
        List<String> calls = new ArrayList<>();
        AroundScope notSupported = new AroundScope() {
            @Override
            public boolean supports(Object source) {
                return false;
            }

            @Override
            public void around(Runnable runnable) {
                calls.add("notSupported");
                runnable.run();
            }
        };

        AroundScope.run(Collections.singletonList(notSupported), this, () -> calls.add("scope"));

        assertThat(calls).containsExactly("scope");
    }

    private AroundScope around(List<String> calls, String name) {
        return new AroundScope() {
            @Override
            public boolean supports(Object source) {
                return true;
            }

            @Override
            public void around(Runnable runnable) {
                calls.add(name);
                runnable.run();
            }
        };
    }
}
