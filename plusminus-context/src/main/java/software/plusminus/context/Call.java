package software.plusminus.context;

@FunctionalInterface
public interface Call<R, X extends Throwable> {

    R call() throws X;

}
