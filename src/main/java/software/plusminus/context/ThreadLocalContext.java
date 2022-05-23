package software.plusminus.context;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class ThreadLocalContext implements Context {
    
    private ThreadLocal<Map<?, ?>> threadLocal = new ThreadLocal<>();

    @Nullable
    @Override
    public String get(String key) {
        return getString(key);
    }

    @Nullable
    @Override
    public <T> T get(String key, Class<T> type) {
        return getObject(key, type);
    }

    @Nullable
    @Override
    public String get(Enum<?> key) {
        return getString(key);
    }

    @Nullable
    @Override
    public <T> T get(Enum<?> key, Class<T> type) {
        return getObject(key, type);
    }

    @Nullable
    @Override
    public <T> T get(Class<T> type) {
        return getObject(type, type);
    }

    @Override
    public <T> void set(String key, T value) {
        setObject(key, value);
    }

    @Override
    public <T> void set(Enum<?> key, T value) {
        setObject(key, value);
    }

    @Override
    public <T> void set(T value) {
        setObject(value.getClass(), value);
    }

    private String getString(Object key) {
        Object value = getMap().get(key);
        if (value == null) {
            return null;
        }
        return value.toString();
    }
    
    private <T> T getObject(Object key, Class<T> type) {
        Object value = getMap().get(key);
        if (value == null) {
            return null;
        }
        return type.cast(value);
    }
    
    private void setObject(Object key, Object value) {
        Map<Object, Object> map = (Map<Object, Object>) getMap();
        map.put(key, value);
    }
    
    private Map<?, ?> getMap() {
        Map<?, ?> map = threadLocal.get();
        if (map == null) {
            map = new HashMap<>();
            threadLocal.set(map);
        }
        return map;
    }
}
