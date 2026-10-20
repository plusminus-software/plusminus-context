package software.plusminus.http.context.fixtures;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import software.plusminus.context.Context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class TestDispatchInterceptor implements HandlerInterceptor, WebMvcConfigurer {

    private final Context<HttpServletRequest> requestContext;
    @Getter
    private final Map<DispatcherType, Boolean> requestBound = new ConcurrentHashMap<>();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        requestBound.put(request.getDispatcherType(), requestContext.optional().isPresent());
        return true;
    }
}
