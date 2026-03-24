package software.plusminus.context;

public class WritableThreadLocalContext<T> extends ThreadLocalContext<T> implements WritableContext<T> {

    private ThreadLocal<T> threadLocal;

    public WritableThreadLocalContext(ThreadLocal<T> threadLocal) {
        super(threadLocal);
        this.threadLocal = threadLocal;
    }

    @Override
    public void set(T value) {
        if (threadLocal.get() != null) {
            throw new IllegalStateException("Context has already been set");
        }
        replace(value);
    }

    @Override
    public void replace(T value) {
        threadLocal.set(value);
    }
}
