package software.plusminus.context.propagation.config;

import lombok.experimental.UtilityClass;
import org.springframework.boot.SpringBootVersion;

@UtilityClass
class TaskDecoratorComposition {

    /* Spring Boot 4+ composes all TaskDecorator beans into a CompositeTaskDecorator,
       earlier versions apply a TaskDecorator bean only if it is unique or @Primary. */
    boolean supported() {
        return supported(SpringBootVersion.getVersion());
    }

    boolean supported(String springBootVersion) {
        if (springBootVersion == null) {
            return false;
        }
        try {
            return Integer.parseInt(springBootVersion.split("\\.")[0]) >= 4;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
