package software.plusminus.scope;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;

import java.util.stream.Stream;

@UtilityClass
public class ExceptionExtractor {

    @SafeVarargs
    public Exception extract(Exception e, Class<? extends Exception>... exceptionsToExtract) {
        if (exceptionsToExtract.length == 0) {
            return e;
        }
        boolean shouldExtract = Stream.of(exceptionsToExtract)
                .anyMatch(type -> e.getClass() == type);
        if (shouldExtract) {
            Exception extracted = extract(e);
            if (extracted != null) {
                return extract(extracted, exceptionsToExtract);
            }
        }
        return e;
    }

    @Nullable
    private Exception extract(Exception e) {
        Throwable cause = e.getCause();
        if (cause instanceof Exception) {
            return (Exception) cause;
        } else {
            return null;
        }
    }
}
