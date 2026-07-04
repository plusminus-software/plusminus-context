package software.plusminus.http.context;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.NestedServletException;
import software.plusminus.context.ClearableContext;
import software.plusminus.context.Context;
import software.plusminus.context.WritableContext;
import software.plusminus.scope.ScopeRunner;

import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpFilter extends OncePerRequestFilter {

    private final ScopeRunner scopeRunner;
    private final WritableContext<HttpServletRequest> httpServletRequestContext;
    private final WritableContext<HttpServletResponse> httpServletResponseContext;
    private List<ClearableContext<?>> contextsToClear;

    @Autowired
    void init(List<Context<?>> contexts) {
        this.contextsToClear = contexts.stream()
                .filter(ClearableContext.class::isInstance)
                .map(context -> (ClearableContext<?>) context)
                .collect(Collectors.toList());
    }

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
