package software.plusminus.context;

import javax.annotation.Nullable;

public interface Context {
    
    @Nullable
    String get(String key);

    @Nullable
    <T> T get(String key, Class<T> type);

    @Nullable
    String get(Enum<?> key);

    @Nullable
    <T> T get(Enum<?> key, Class<T> type);

    @Nullable
    <T> T get(Class<T> type);
    
    <T> void set(String key, T value);
    
    <T> void set(Enum<?> key, T value);
    
    <T> void set(T value);
    
}
