package software.plusminus.http.context;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.NestedServletException;
import software.plusminus.context.ClearableContext;
import software.plusminus.context.WritableContext;
import software.plusminus.scope.ScopeRunner;

import java.util.List;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpFilter extends OncePerRequestFilter {

    private ScopeRunner scopeRunner;
    private List<ClearableContext<?>> contextsToClear;
    private WritableContext<HttpServletRequest> httpServletRequestContext;
    private WritableContext<HttpServletResponse> httpServletResponseContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) {
        try {
            httpServletRequestContext.set(request);
            httpServletResponseContext.set(response);
            scopeRunner.run(
                    this,
                    () -> filterChain.doFilter(httpServletRequestContext.get(), httpServletResponseContext.get()),
                    NestedServletException.class
            );
        } finally {
            contextsToClear.forEach(ClearableContext::clear);
        }
    }
}
