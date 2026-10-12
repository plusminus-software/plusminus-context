package software.plusminus.http.context.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.Context;
import software.plusminus.http.context.HttpInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@ComponentScan("software.plusminus.http.context")
public class HttpContextAutoconfig {

    @Bean
    Context<HttpServletRequest> httpServletRequestContext() {
        return Context.of();
    }

    @Bean
    Context<HttpServletResponse> httpServletResponseContext() {
        return Context.of();
    }

    @Bean
    Context<Object> handlerContext(Context<HttpServletRequest> httpServletRequestContext) {
        return httpServletRequestContext.map(request -> request.getAttribute(HttpInterceptor.HANDLER));
    }

    @Bean
    Context<HandlerMethod> handlerMethodContext(Context<Object> handlerContext) {
        return handlerContext.map(handler -> handler instanceof HandlerMethod ? (HandlerMethod) handler : null);
    }
}
