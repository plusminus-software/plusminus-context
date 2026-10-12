package software.plusminus.context.propagation;

import lombok.AllArgsConstructor;
import org.springframework.core.task.TaskDecorator;
import software.plusminus.context.Context;

@AllArgsConstructor
public class ContextPropagationTaskDecorator implements TaskDecorator {

    private TaskDecorator delegate;

    public ContextPropagationTaskDecorator() {
        this(task -> task);
    }

    @Override
    public Runnable decorate(Runnable task) {
        return Context.snapshot().wrap(delegate.decorate(task));
    }
}
