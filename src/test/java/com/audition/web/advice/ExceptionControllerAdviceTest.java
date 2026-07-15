package com.audition.web.advice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.client.HttpClientErrorException;

@ExtendWith(MockitoExtension.class)
class ExceptionControllerAdviceTest {

    @Mock
    private AuditionLogger auditionLogger;

    @InjectMocks
    private ExceptionControllerAdvice exceptionControllerAdvice;

    @Test
    void handleSystemException_returnsProblemDetailAndLogs() {
        final SystemException exception =
            new SystemException("Cannot find post", "Resource Not Found", 404);

        final ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        assertEquals(404, result.getStatus());
        assertEquals("Resource Not Found", result.getTitle());
        assertEquals("Cannot find post", result.getDetail());
        verify(auditionLogger).logStandardProblemDetail(any(Logger.class), eq(result), eq(exception));
    }

    @Test
    void handleSystemException_badRequest_returnsBadRequest() {
        final SystemException exception =
            new SystemException("Invalid post id: abc", "Bad Request", 400);

        final ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        assertEquals(400, result.getStatus());
        assertEquals("Bad Request", result.getTitle());
        verify(auditionLogger).logStandardProblemDetail(any(Logger.class), eq(result), eq(exception));
    }

    @Test
    void handleMainException_returnsInternalServerErrorAndLogs() {
        final Exception exception = new RuntimeException("Unexpected failure");

        final ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        assertEquals(500, result.getStatus());
        assertEquals(ExceptionControllerAdvice.DEFAULT_TITLE, result.getTitle());
        assertEquals("Unexpected failure", result.getDetail());
        verify(auditionLogger).logStandardProblemDetail(any(Logger.class), eq(result), eq(exception));
    }

    @Test
    void handleHttpClientException_returnsProblemDetailFromStatusCode() {
        final HttpClientErrorException exception =
            new HttpClientErrorException(HttpStatus.NOT_FOUND);

        final ProblemDetail result = exceptionControllerAdvice.handleHttpClientException(exception);

        assertEquals(404, result.getStatus());
        assertEquals(ExceptionControllerAdvice.DEFAULT_TITLE, result.getTitle());
    }

}
