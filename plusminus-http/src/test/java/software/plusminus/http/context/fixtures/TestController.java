package software.plusminus.http.context.fixtures;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.Context;

import java.util.concurrent.Callable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@AllArgsConstructor
public class TestController {

    private Context<HttpServletRequest> request;
    private Context<HttpServletResponse> response;
    private Context<Object> handler;
    private Context<HandlerMethod> handlerMethod;
    private TestAsyncProbe asyncProbe;

    @GetMapping("/ok")
    public String ok() {
        return "ok";
    }

    @GetMapping("/exception")
    public String exception() {
        throw new IllegalStateException("Test exception");
    }

    @GetMapping("/context")
    public String context() {
        response.get().setHeader("handler", handler.get().getClass().getSimpleName());
        return request.get().getRequestURI() + " " + handlerMethod.get().getMethod().getName();
    }

    @GetMapping("/async")
    public Callable<String> async() {
        return () -> {
            asyncProbe.finished();
            return "async";
        };
    }

    @GetMapping("/async-exception")
    public Callable<String> asyncException() {
        return () -> {
            throw new IllegalStateException("Async exception");
        };
    }
}
