package software.plusminus.context;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ThreadLocalUtil {

    public <T> ThreadLocal<T> createThreadLocal(Supplier<T> supplier, boolean inheritable) {
        return inheritable ? createInheritableThreadLocal(supplier) : ThreadLocal.withInitial(supplier);
    }

    private <T> InheritableThreadLocal<T> createInheritableThreadLocal(Supplier<T> supplier) {
        return new InheritableThreadLocal<T>() {
            @Override
            protected T initialValue() {
                return supplier.get();
            }
        };
    }
}
