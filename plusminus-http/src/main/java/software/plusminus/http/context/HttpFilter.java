package software.plusminus.http.context;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.NestedServletException;
import software.plusminus.context.Context;
import software.plusminus.scope.ScopeCompletion;
import software.plusminus.scope.ScopeRunner;
import software.plusminus.scope.ThrowingRunnable;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@AllArgsConstructor
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class HttpFilter extends OncePerRequestFilter {

    private static final String ASYNC_FAILURE = HttpFilter.class.getName() + ".asyncFailure";

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
        ThrowingRunnable chain = () -> filterChain.doFilter(
                httpServletRequestContext.get(),
                httpServletResponseContext.get()
        );
        DispatcherType dispatcherType = request.getDispatcherType();
        Context.where(httpServletRequestContext, request)
                .where(httpServletResponseContext, response)
                .run(() -> {
                    if (dispatcherType == DispatcherType.REQUEST) {
                        runScope(request, chain);
                    } else if (dispatcherType == DispatcherType.ASYNC) {
                        runAsyncDispatch(request, chain);
                    } else {
                        scopeRunner.around(this, chain);
                    }
                });
    }

    private void runScope(HttpServletRequest request, ThrowingRunnable chain) {
        ScopeCompletion completion = scopeRunner.runDeferred(this, chain, NestedServletException.class);
        if (request.isAsyncStarted()) {
            request.getAsyncContext().addListener(new CompletionListener(request, completion));
        } else {
            completion.complete();
        }
    }

    @SuppressWarnings("PMD.AvoidCatchingThrowable")
    private void runAsyncDispatch(HttpServletRequest request, ThrowingRunnable chain) {
        try {
            scopeRunner.around(this, chain);
        } catch (Throwable t) {
            request.setAttribute(ASYNC_FAILURE, t);
            throw t;
        }
    }

    /**
     * Finishes the scope of an async request when the request really ends, with the values bound
     * during the request.
     */
    private static final class CompletionListener implements AsyncListener {

        private final HttpServletRequest request;
        private final ScopeCompletion completion;
        private final Runnable finish;
        private volatile Throwable failure;

        private CompletionListener(HttpServletRequest request, ScopeCompletion completion) {
            this.request = request;
            this.completion = completion;
            this.finish = Context.snapshot().wrap(this::finishNow);
        }

        @Override
        public void onComplete(AsyncEvent event) {
            Object dispatchFailure = request.getAttribute(ASYNC_FAILURE);
            if (failure == null && dispatchFailure instanceof Throwable) {
                failure = (Throwable) dispatchFailure;
            }
            finish.run();
        }

        @Override
        public void onError(AsyncEvent event) {
            failure = event.getThrowable();
            finish.run();
        }

        @Override
        public void onTimeout(AsyncEvent event) {
            // Spring MVC handles the timeout; the request then completes or fails as usual.
        }

        @Override
        public void onStartAsync(AsyncEvent event) {
            event.getAsyncContext().addListener(this);
        }

        private void finishNow() {
            Throwable result = failure;
            if (result == null) {
                completion.complete();
            } else {
                completion.fail(result);
            }
        }
    }
}
