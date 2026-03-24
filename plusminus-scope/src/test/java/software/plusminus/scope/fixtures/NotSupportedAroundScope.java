package software.plusminus.scope.fixtures;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import software.plusminus.scope.AroundScope;

@Component
@Order(3)
public class NotSupportedAroundScope implements AroundScope {

    @Override
    public boolean supports(Object source) {
        return false;
    }

    @Override
    public void around(Runnable runnable) {
        runnable.run();
    }
}
