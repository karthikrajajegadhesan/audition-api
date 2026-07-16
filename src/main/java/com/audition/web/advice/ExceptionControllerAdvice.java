package com.audition.web.advice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import io.micrometer.common.util.StringUtils;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Global exception handler that maps application and upstream errors to
 * ProblemDetail responses with consistent titles and HTTP status codes.
 */
@ControllerAdvice
@Getter
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String ERROR_MESSAGE =
        " Error Code from Exception could not be mapped to a valid HttpStatus Code - ";
    private static final String DEFAULT_MESSAGE =
        "API Error occurred. Please contact support or administrator.";

    private final AuditionLogger logger;

    public ExceptionControllerAdvice(final AuditionLogger logger) {
        super();
        this.logger = logger;
    }

    /** Maps upstream HTTP client errors to problem details using the remote status code. */
    @ExceptionHandler(HttpClientErrorException.class)
    ProblemDetail handleHttpClientException(final HttpClientErrorException exception) {
        return createProblemDetail(exception, exception.getStatusCode());
    }

    /** Fallback handler for uncaught exceptions. */
    @ExceptionHandler(Exception.class)
    ProblemDetail handleMainException(final Exception exception) {
        final HttpStatusCode status = getHttpStatusCodeFromException(exception);
        final ProblemDetail problemDetail = createProblemDetail(exception, status);
        logger.logStandardProblemDetail(LOG, problemDetail, exception);
        return problemDetail;
    }

    /** Maps application SystemException instances to problem details. */
    @ExceptionHandler(SystemException.class)
    ProblemDetail handleSystemException(final SystemException exception) {
        final HttpStatusCode status = getHttpStatusCodeFromSystemException(exception);
        final ProblemDetail problemDetail = createProblemDetail(exception, status);
        logger.logStandardProblemDetail(LOG, problemDetail, exception);
        return problemDetail;
    }

    private ProblemDetail createProblemDetail(final Exception exception,
        final HttpStatusCode statusCode) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setDetail(getMessageFromException(exception));
        if (exception instanceof SystemException) {
            problemDetail.setTitle(((SystemException) exception).getTitle());
        } else {
            problemDetail.setTitle(DEFAULT_TITLE);
        }
        return problemDetail;
    }

    private String getMessageFromException(final Exception exception) {
        if (StringUtils.isNotBlank(exception.getMessage())) {
            return exception.getMessage();
        }
        return DEFAULT_MESSAGE;
    }

    private HttpStatusCode getHttpStatusCodeFromSystemException(final SystemException exception) {
        try {
            return HttpStatusCode.valueOf(exception.getStatusCode());
        } catch (final IllegalArgumentException exceptionDuringMapping) {
            if (LOG.isInfoEnabled()) {
                logger.info(LOG, ERROR_MESSAGE + exception.getStatusCode());
            }
            return INTERNAL_SERVER_ERROR;
        }
    }

    private HttpStatusCode getHttpStatusCodeFromException(final Exception exception) {
        if (exception instanceof HttpClientErrorException) {
            return ((HttpClientErrorException) exception).getStatusCode();
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            return METHOD_NOT_ALLOWED;
        }
        return INTERNAL_SERVER_ERROR;
    }
}
