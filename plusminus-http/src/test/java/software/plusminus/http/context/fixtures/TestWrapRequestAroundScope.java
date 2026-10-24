package software.plusminus.http.context.fixtures;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import software.plusminus.context.Context;
import software.plusminus.scope.AroundScope;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@Component
@AllArgsConstructor
public class TestWrapRequestAroundScope implements AroundScope {

    private Context<HttpServletRequest> requestContext;

    @Override
    public boolean supports(Object source) {
        return true;
    }

    @Override
    public void around(Runnable runnable) {
        HttpServletRequest request = requestContext.get();
        if (!isWrapped(request)) {
            runnable.run();
            return;
        }
        Context.where(requestContext, (HttpServletRequest) new TestWrappedRequest(request))
                .run(runnable);
    }

    private static boolean isWrapped(HttpServletRequest request) {
        Object errorUri = request.getAttribute("javax.servlet.error.request_uri");
        String uri = errorUri == null ? request.getRequestURI() : errorUri.toString();
        return uri.startsWith("/wrapped");
    }

    public static class TestWrappedRequest extends HttpServletRequestWrapper {

        public TestWrappedRequest(HttpServletRequest request) {
            super(request);
        }
    }
}
