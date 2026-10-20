package software.plusminus.context;

public interface UncachedContext<T> extends Context<T> {

    @Override
    default T get() {
        Scope scope = Scope.current();
        if (scope != null && scope.contains(this)) {
            return scope.get(this);
        }
        return supply();
    }

}
