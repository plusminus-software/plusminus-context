package software.plusminus.context;

public class ThreadLocalContext<T> implements Context<T>, ClearableContext<T> {

    private ThreadLocal<T> threadLocal;
    private boolean inheritable;

    public ThreadLocalContext(ThreadLocal<T> threadLocal) {
        this.threadLocal = threadLocal;
        this.inheritable = threadLocal instanceof InheritableThreadLocal;
    }

    @Override
    public T get() {
        return threadLocal.get();
    }

    @Override
    public boolean isInheritable() {
        return inheritable;
    }

    @Override
    public void clear() {
        threadLocal.remove();
    }
}
