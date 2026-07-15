package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

@Component
@Getter
public class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RestTemplateLoggingInterceptor.class);

    private final AuditionLogger auditionLogger;

    public RestTemplateLoggingInterceptor(final AuditionLogger auditionLogger) {
        this.auditionLogger = auditionLogger;
    }

    @Override
    public ClientHttpResponse intercept(final HttpRequest request, final byte[] body,
        final ClientHttpRequestExecution execution) throws IOException {
        if (LOG.isInfoEnabled()) {
            auditionLogger.info(LOG, String.format("RestTemplate request: %s %s",
                request.getMethod(), request.getURI()));
            if (body.length > 0) {
                auditionLogger.info(LOG, "RestTemplate request body: {}",
                    new String(body, StandardCharsets.UTF_8));
            }
        }

        final ClientHttpResponse response = execution.execute(request, body);

        if (LOG.isInfoEnabled()) {
            auditionLogger.info(LOG, String.format("RestTemplate response: %s %s",
                response.getStatusCode(), request.getURI()));
            final String responseBody =
                StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
            if (!responseBody.isEmpty()) {
                auditionLogger.info(LOG, "RestTemplate response body: {}", responseBody);
            }
        }

        return response;
    }

}
