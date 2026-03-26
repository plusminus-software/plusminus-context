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
import software.plusminus.context.WritableContext;
import software.plusminus.http.context.fixtures.TestScopeListener;
import software.plusminus.scope.events.ScopeCompletedEvent;
import software.plusminus.scope.events.ScopeFailedEvent;
import software.plusminus.scope.events.ScopeFinalizedEvent;
import software.plusminus.scope.events.ScopeStartedEvent;

import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static software.plusminus.check.Checks.check;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpFilterTest {

    @LocalServerPort
    private int port;

    @SpyBean
    private WritableContext<HttpServletRequest> requestContext;
    @SpyBean
    private WritableContext<HttpServletResponse> responseContext;
    @SpyBean
    private TestScopeListener listener;

    @Autowired
    private TestRestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<ScopeFailedEvent<?>> failedEventCaptor;

    @Test
    void writableContext() {
        String url = "http://localhost:" + port + "/ok";

        String response = restTemplate.getForObject(url, String.class);

        check(response).is("ok");
        verify(requestContext).set(any(HttpServletRequest.class));
        verify(responseContext).set(any(HttpServletResponse.class));
    }

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
        String url = "http://localhost:" + port + "/exception";

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
        check(exception.getMessage()).is("Test exception");
    }
}