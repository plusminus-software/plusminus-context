package software.plusminus.http.context;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.NestedServletException;
import software.plusminus.context.Context;
import software.plusminus.scope.ScopeRunner;
import software.plusminus.scope.ThrowingRunnable;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpFilter extends OncePerRequestFilter {

    private ScopeRunner scopeRunner;
    private Context<HttpServletRequest> httpServletRequestContext;
    private Context<HttpServletResponse> httpServletResponseContext;

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return false;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) {
        ThrowingRunnable chain = () -> filterChain.doFilter(request, response);
        Runnable body = request.getDispatcherType() == DispatcherType.REQUEST
                ? () -> scopeRunner.run(this, chain, NestedServletException.class)
                : chain.asRunnable();
        Context.where(httpServletRequestContext, () -> request)
                .where(httpServletResponseContext, () -> response)
                .run(body);
    }
}
