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
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.WritableContext;
import software.plusminus.http.context.fixtures.TestInvocationListener;
import software.plusminus.scope.events.InvocationCompletedEvent;
import software.plusminus.scope.events.InvocationFailedEvent;
import software.plusminus.scope.events.InvocationFinalizedEvent;
import software.plusminus.scope.events.InvocationStartedEvent;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static software.plusminus.check.Checks.check;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HttpInterceptorTest {

    @LocalServerPort
    private int port;

    @SpyBean
    private WritableContext<HandlerMethod> handlerMethodContext;
    @SpyBean
    private TestInvocationListener listener;

    @Autowired
    private TestRestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<InvocationFailedEvent<HandlerMethod, ?>> failedEventCaptor;

    @Test
    void writableContext() {
        String url = "http://localhost:" + port + "/ok";

        String response = restTemplate.getForObject(url, String.class);

        assertThat(response).isEqualTo("ok");
        verify(handlerMethodContext).set(any(HandlerMethod.class));
    }

    @Test
    void ok() {
        String url = "http://localhost:" + port + "/ok";

        String response = restTemplate.getForObject(url, String.class);

        check(response).is("ok");
        InOrder inOrder = inOrder(listener);
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            inOrder.verify(listener).started(any(InvocationStartedEvent.class));
            inOrder.verify(listener).completed(any(InvocationCompletedEvent.class));
            inOrder.verify(listener, never()).failed(any());
            inOrder.verify(listener).finalized(any(InvocationFinalizedEvent.class));
        });
    }

    @Test
    void exception() {
        String url = "http://localhost:" + port + "/exception";

        ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);

        check(response.getStatusCode()).is(HttpStatus.INTERNAL_SERVER_ERROR);
        InOrder inOrder = inOrder(listener);
        await().atMost(1, TimeUnit.SECONDS).untilAsserted(() -> {
            inOrder.verify(listener).started(any(InvocationStartedEvent.class));
            inOrder.verify(listener, never()).completed(any());
            inOrder.verify(listener).failed(failedEventCaptor.capture());
            verify(listener).failedWithSpecificException(any(InvocationFailedEvent.class));
            verify(listener, never()).failedWithUnknownException(any());
            inOrder.verify(listener).finalized(any(InvocationFinalizedEvent.class));
        });
        Exception exception = failedEventCaptor.getValue().getException();
        Class exceptionType = exception.getClass();
        check(exceptionType).isEqual(IllegalStateException.class);
        check(exception.getMessage()).is("Test exception");
    }
}