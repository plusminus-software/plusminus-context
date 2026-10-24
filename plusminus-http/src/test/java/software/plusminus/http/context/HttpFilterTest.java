package software.plusminus.http.context;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.plusminus.context.Context;
import software.plusminus.http.context.fixtures.TestAsyncProbe;
import software.plusminus.http.context.fixtures.TestDispatchInterceptor;
import software.plusminus.http.context.fixtures.TestScopeListener;
import software.plusminus.scope.events.ScopeCompletedEvent;
import software.plusminus.scope.events.ScopeFailedEvent;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;

import java.util.concurrent.TimeUnit;
import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequest;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static software.plusminus.check.Checks.check;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpFilterTest {

    @LocalServerPort
    private int port;

    @Autowired
    private Context<HttpServletRequest> requestContext;
    @SpyBean
    private TestScopeListener listener;
    @SpyBean
    private TestAsyncProbe asyncProbe;
    @Autowired
    private TestDispatchInterceptor dispatchInterceptor;

    @Autowired
    private TestRestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<ScopeFailedEvent<?>> failedEventCaptor;

    @Test
    void ok() {
        String url = "http://localhost:" + port + "/ok";

        String response = restTemplate.getForObject(url, String.class);

        check(response).is("ok");
        InOrder inOrder = inOrder(listener);
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            inOrder.verify(listener).started(any(ScopeStartedEvent.class));
            inOrder.verify(listener).completed(any(ScopeCompletedEvent.class));
            verify(listener, never()).failed(any());
            inOrder.verify(listener).finalized(any(ScopeFinalizedEvent.class));
        });
    }

    @Test
    void exception() {
        checkFailedScope("/exception", "Test exception");
    }

    @Test
    void context() {
        String url = "http://localhost:" + port + "/context";

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        check(response.getBody()).is("/context context");
        check(response.getHeaders().getFirst("handler")).is("HandlerMethod");
        assertThrows(IllegalStateException.class, requestContext::get);
    }

    @Test
    void asyncDispatch() {
        String url = "http://localhost:" + port + "/async";

        String response = restTemplate.getForObject(url, String.class);

        check(response).is("async");
        InOrder inOrder = inOrder(listener, asyncProbe);
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            inOrder.verify(listener).started(any(ScopeStartedEvent.class));
            inOrder.verify(asyncProbe).finished();
            inOrder.verify(listener).completed(any(ScopeCompletedEvent.class));
            verify(listener, never()).failed(any());
            inOrder.verify(listener).finalized(any(ScopeFinalizedEvent.class));
        });
    }

    @Test
    void asyncException() {
        checkFailedScope("/async-exception", "Async exception");
    }

    private void checkFailedScope(String path, String message) {
        String url = "http://localhost:" + port + path;

        ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);

        check(response.getStatusCode()).is(HttpStatus.INTERNAL_SERVER_ERROR);
        InOrder inOrder = inOrder(listener);
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            inOrder.verify(listener).started(any(ScopeStartedEvent.class));
            verify(listener, never()).completed(any());
            inOrder.verify(listener).failed(failedEventCaptor.capture());
            verify(listener, never()).unknownFailed(any());
            inOrder.verify(listener).finalized(any(ScopeFinalizedEvent.class));
        });
        Exception exception = failedEventCaptor.getValue().getException();
        Class exceptionType = exception.getClass();
        check(exceptionType).isEqual(IllegalStateException.class);
        check(exception.getMessage()).is(message);
    }

    @Test
    void requestContextInAsyncDispatch() {
        String url = "http://localhost:" + port + "/async";
        dispatchInterceptor.getRequestBound().clear();

        restTemplate.getForObject(url, String.class);

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            check(dispatchInterceptor.getRequestBound().get(DispatcherType.REQUEST)).is(true);
            check(dispatchInterceptor.getRequestBound().get(DispatcherType.ASYNC)).is(true);
        });
    }

    @Test
    void requestContextInErrorDispatch() {
        String url = "http://localhost:" + port + "/exception";
        dispatchInterceptor.getRequestBound().clear();

        restTemplate.getForEntity(url, Object.class);

        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            check(dispatchInterceptor.getRequestBound().get(DispatcherType.REQUEST)).is(true);
            check(dispatchInterceptor.getRequestBound().get(DispatcherType.ERROR)).is(true);
        });
    }
}