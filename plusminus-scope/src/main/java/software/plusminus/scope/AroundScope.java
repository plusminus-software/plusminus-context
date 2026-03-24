package software.plusminus.scope;

import java.util.List;
import java.util.stream.Collectors;

public interface AroundScope {

    boolean supports(Object source);

    void around(Runnable runnable);

    static Runnable around(List<AroundScope> arounds, Object source, Runnable runnable) {
        if (arounds.isEmpty()) {
            return runnable;
        }
        Runnable result = runnable;
        List<AroundScope> filteredArounds = arounds.stream()
                .filter(around -> around.supports(source))
                .collect(Collectors.toList());
        for (int i = filteredArounds.size() - 1; i >= 0; i--) {
            Runnable current = result;
            AroundScope around = filteredArounds.get(i);
            result = () -> around.around(current);
        }
        return result;
    }

    static void run(List<AroundScope> arounds, Object source, Runnable runnable) {
        around(arounds, source, runnable).run();
    }
}
