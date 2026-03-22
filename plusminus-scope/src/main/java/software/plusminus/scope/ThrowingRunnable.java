package software.plusminus.scope;

@FunctionalInterface
public interface ThrowingRunnable {

    @SuppressWarnings("java:S112")
    void run() throws Exception;

    default Runnable asRunnable() {
        return () -> {
            try {
                run();
            } catch (Exception e) {
                sneakyThrow(e);
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <E extends Throwable> void sneakyThrow(Throwable e) throws E {
        throw (E) e;
    }

    static ThrowingRunnable of(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                ThrowingRunnable.sneakyThrow(e);
            }
        };
    }
}
