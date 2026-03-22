package software.plusminus.context.http;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import software.plusminus.context.Context;
import software.plusminus.context.WritableContext;
import software.plusminus.scope.ScopeRunner;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpFilter extends OncePerRequestFilter {

    private ScopeRunner scopeRunner;
    private WritableContext<HttpServletRequest> httpServletRequestContext;
    private WritableContext<HttpServletResponse> httpServletResponseContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) {
        try {
            Context.init();
            httpServletRequestContext.set(request);
            httpServletResponseContext.set(response);
            scopeRunner.run(this, () -> filterChain.doFilter(
                    httpServletRequestContext.get(),
                    httpServletResponseContext.get()
            ));
        } finally {
            Context.clear();
        }
    }
}
