package software.plusminus.context;

public class ConstantContext<T> implements Context<T> {

    private T value;

    public ConstantContext(T value) {
        this.value = value;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public boolean isInheritable() {
        return true;
    }
}
