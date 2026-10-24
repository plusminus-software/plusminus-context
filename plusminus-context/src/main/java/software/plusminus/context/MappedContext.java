package software.plusminus.context;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
class MappedContext<S, T> implements UncachedContext<T> {

    private final Context<S> source;
    private final Function<? super S, ? extends T> mapper;

    @Override
    public T supply() {
        return mapper.apply(source.get());
    }

    @Override
    public Optional<T> optional() {
        Scope scope = Scope.current();
        if (scope != null && scope.contains(this)) {
            return Optional.ofNullable(scope.get(this));
        }
        return source.optional()
                .map(mapper);
    }
}
