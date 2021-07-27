package software.plusminus.context;

public interface Context<T> {
    
    T get();
    
    void set(T value);
    
}
