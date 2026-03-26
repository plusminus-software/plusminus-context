package software.plusminus.context;

import lombok.Getter;

public class ThreadLocalContext<T> implements Context<T>, ClearableContext<T> {

    private ThreadLocal<T> threadLocal;
    @Getter
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
    public void clear() {
        threadLocal.remove();
    }

}
