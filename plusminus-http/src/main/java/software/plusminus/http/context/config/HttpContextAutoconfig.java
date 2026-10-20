package software.plusminus.http.context.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.Context;
import software.plusminus.http.context.HttpFilter;
import software.plusminus.http.context.HttpInterceptor;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@ComponentScan("software.plusminus.http.context")
public class HttpContextAutoconfig {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    FilterRegistrationBean<HttpFilter> httpFilterRegistration(HttpFilter httpFilter) {
        FilterRegistrationBean<HttpFilter> registration = new FilterRegistrationBean<>(httpFilter);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC, DispatcherType.ERROR);
        return registration;
    }

    @Bean
    Context<HttpServletRequest> httpServletRequestContext() {
        return Context.required();
    }

    @Bean
    Context<HttpServletResponse> httpServletResponseContext() {
        return Context.required();
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
