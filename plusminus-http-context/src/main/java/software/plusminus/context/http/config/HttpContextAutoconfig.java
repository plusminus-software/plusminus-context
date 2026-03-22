package software.plusminus.context.http.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.WritableContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@ComponentScan("software.plusminus.context.http")
public class HttpContextAutoconfig {

    @Bean
    WritableContext<HttpServletRequest> httpServletRequestContext() {
        return WritableContext.of();
    }

    @Bean
    WritableContext<HttpServletResponse> httpServletResponseContext() {
        return WritableContext.of();
    }

    @Bean
    WritableContext<HandlerMethod> handlerMethodContext() {
        return WritableContext.of();
    }

    @Bean
    WritableContext<Object> handlerContext() {
        return WritableContext.of();
    }
}
