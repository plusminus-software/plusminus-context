package software.plusminus.context.http;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import software.plusminus.context.WritableContext;
import software.plusminus.context.http.fixtures.TestListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    private WritableContext<HandlerMethod> handlerMethodContext;

    @Autowired
    private TestListener listener;
    @Autowired
    private TestRestTemplate restTemplate;

    @AfterEach
    void afterEach() {
        listener.calls.clear();
    }

    @Test
    void writableContext() {
        String url = "http://localhost:" + port + "/ok";

        String response = restTemplate.getForObject(url, String.class);

        assertThat(response).isEqualTo("ok");
        verify(requestContext).set(any(HttpServletRequest.class));
        verify(responseContext).set(any(HttpServletResponse.class));
        verify(handlerMethodContext).set(any(HandlerMethod.class));
    }

    @Test
    void ok() {
        String url = "http://localhost:" + port + "/ok";

        String response = restTemplate.getForObject(url, String.class);

        assertThat(response).isEqualTo("ok");
        check(listener.calls).is("started", "completed", "finalized");
    }

    @Test
    void exception() {
        String url = "http://localhost:" + port + "/exception";

        ResponseEntity<?> response = restTemplate.getForEntity(url, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        check(listener.calls).is("started", "failed", "finalized");
    }
}