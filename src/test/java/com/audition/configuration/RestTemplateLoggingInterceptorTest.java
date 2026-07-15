package com.audition.configuration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.audition.common.logging.AuditionLogger;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;

@ExtendWith(MockitoExtension.class)
class RestTemplateLoggingInterceptorTest {

    @Mock
    private AuditionLogger auditionLogger;

    @Mock
    private ClientHttpRequestExecution execution;

    @Mock
    private ClientHttpResponse response;

    @InjectMocks
    private RestTemplateLoggingInterceptor loggingInterceptor;

    @Test
    void intercept_logsRequestAndResponse() throws Exception {
        final HttpRequest request = new MockClientHttpRequest(HttpMethod.GET,
            java.net.URI.create("https://example.com/posts"));
        final byte[] body = "request-body".getBytes(StandardCharsets.UTF_8);
        when(execution.execute(request, body)).thenReturn(response);
        when(response.getBody()).thenReturn(
            new java.io.ByteArrayInputStream("response-body".getBytes(StandardCharsets.UTF_8)));

        loggingInterceptor.intercept(request, body, execution);

        verify(execution).execute(request, body);
        verify(auditionLogger, atLeastOnce()).info(any(), any(String.class));
        verify(auditionLogger, atLeastOnce()).info(any(), any(String.class), any());
    }
}
