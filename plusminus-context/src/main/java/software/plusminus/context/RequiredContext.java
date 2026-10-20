package software.plusminus.context;

import java.util.NoSuchElementException;
import java.util.Optional;

class RequiredContext<T> implements Context<T> {

    @Override
    public T supply() {
        throw new NoSuchElementException("No value in context");
    }

    @Override
    public T get() {
        if (Scope.current() == null) {
            throw new IllegalStateException("No active scope");
        }
        return Context.super.get();
    }

    @Override
    public Optional<T> optional() {
        Scope scope = Scope.current();
        if (scope == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(scope.get(this));
    }
}
