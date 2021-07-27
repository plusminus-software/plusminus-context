package software.plusminus.context;

import lombok.experimental.Delegate;

public class ThreadLocalContext<T> implements Context<T> {
    
    @Delegate
    private ThreadLocal<T> threadLocal = new ThreadLocal<>();
    
}
