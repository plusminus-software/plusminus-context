package software.plusminus.http.context;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.NestedServletException;
import software.plusminus.scope.ExceptionExtractor;
import software.plusminus.scope.events.InvocationCompletedEvent;
import software.plusminus.scope.events.InvocationFailedEvent;
import software.plusminus.scope.events.InvocationFinalizedEvent;
import software.plusminus.scope.events.InvocationStartedEvent;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    public static final String HANDLER = HttpInterceptor.class.getName() + ".handler";

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this).order(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        request.setAttribute(HANDLER, handler);
        if (request.getDispatcherType() == DispatcherType.REQUEST) {
            InvocationStartedEvent<?> event = new InvocationStartedEvent<>(handler);
            try {
                eventPublisher.publishEvent(event);
            } catch (RuntimeException e) {
                publishAfterFailure(new InvocationFailedEvent<>(handler, e), e);
                publishAfterFailure(new InvocationFinalizedEvent<>(handler), e);
                throw e;
            }
            if (event.isIntercepted()) {
                eventPublisher.publishEvent(new InvocationFinalizedEvent<>(handler));
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        if (!endsInvocation(request)) {
            return;
        }
        eventPublisher.publishEvent(new InvocationCompletedEvent<>(handler, modelAndView));
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        if (!endsInvocation(request)) {
            return;
        }
        if (ex != null) {
            Exception extractedException = ExceptionExtractor.extract(ex, NestedServletException.class);
            eventPublisher.publishEvent(new InvocationFailedEvent<>(handler, extractedException));
        }
        eventPublisher.publishEvent(new InvocationFinalizedEvent<>(handler));
    }

    /**
     * An invocation starts on the REQUEST dispatch. Spring MVC ends it on the same dispatch, or on the
     * ASYNC dispatch when the handler returned an async result.
     */
    private static boolean endsInvocation(HttpServletRequest request) {
        DispatcherType type = request.getDispatcherType();
        return type == DispatcherType.REQUEST || type == DispatcherType.ASYNC;
    }

    private void publishAfterFailure(Object event, RuntimeException failure) {
        try {
            eventPublisher.publishEvent(event);
        } catch (RuntimeException e) {
            failure.addSuppressed(e);
        }
    }
}
