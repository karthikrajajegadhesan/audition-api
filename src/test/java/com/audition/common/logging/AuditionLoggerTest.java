package com.audition.common.logging;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.http.ProblemDetail;

class AuditionLoggerTest {

    private final AuditionLogger auditionLogger = new AuditionLogger();

    @Test
    void info_logsWhenEnabled() {
        final Logger logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(true);

        auditionLogger.info(logger, "info-message");

        verify(logger).info("info-message");
    }

    @Test
    void logStandardProblemDetail_logsFormattedProblemDetail() {
        final Logger logger = mock(Logger.class);
        when(logger.isErrorEnabled()).thenReturn(true);
        final ProblemDetail problemDetail = ProblemDetail.forStatus(404);
        problemDetail.setTitle("Not Found");
        problemDetail.setDetail("Missing resource");
        final RuntimeException exception = new RuntimeException("failure");

        auditionLogger.logStandardProblemDetail(logger, problemDetail, exception);

        verify(logger).error(
            "ProblemDetail [status=404, title=Not Found, detail=Missing resource]", exception);
    }

    @Test
    void logHttpStatusCodeError_logsFormattedMessage() {
        final Logger logger = mock(Logger.class);
        when(logger.isErrorEnabled()).thenReturn(true);

        auditionLogger.logHttpStatusCodeError(logger, "Bad input", 400);

        verify(logger).error("Error [code=400, message=Bad input]\n");
    }
}
