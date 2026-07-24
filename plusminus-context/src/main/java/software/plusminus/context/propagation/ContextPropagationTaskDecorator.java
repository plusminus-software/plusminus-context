package software.plusminus.context.propagation;

import org.springframework.core.task.TaskDecorator;
import software.plusminus.context.Context;
import software.plusminus.context.ContextPropagationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContextPropagationTaskDecorator implements TaskDecorator {

    private List<Context<?>> contexts;
    private TaskDecorator delegate;

    public ContextPropagationTaskDecorator(List<Context<?>> contexts) {
        this(contexts, null);
    }

    public ContextPropagationTaskDecorator(List<Context<?>> contexts, TaskDecorator delegate) {
        this.contexts = contexts.stream()
                .filter(ContextPropagationUtil::isPropagatable)
                .collect(Collectors.toList());
        this.delegate = delegate;
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        Runnable target = delegate == null ? runnable : delegate.decorate(runnable);
        Object[] captured = contexts.stream()
                .map(Context::get)
                .toArray();
        return () -> {
            List<Context<?>> propagated = new ArrayList<>();
            for (int i = 0; i < captured.length; i++) {
                Context<?> context = contexts.get(i);
                if (ContextPropagationUtil.replace(context, captured[i])) {
                    propagated.add(context);
                }
            }
            try {
                target.run();
            } finally {
                propagated.forEach(ContextPropagationUtil::clear);
            }
        };
    }
}
