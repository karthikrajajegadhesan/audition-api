package com.audition.configuration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResponseHeaderInjectorTest {

    @Mock
    private Tracer tracer;

    @Mock
    private Span span;

    @Mock
    private TraceContext traceContext;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ResponseHeaderInjector responseHeaderInjector;

    @Test
    void doFilterInternal_addsTraceHeadersWhenSpanPresent() throws Exception {
        when(tracer.currentSpan()).thenReturn(span);
        when(span.context()).thenReturn(traceContext);
        when(traceContext.traceId()).thenReturn("trace-123");
        when(traceContext.spanId()).thenReturn("span-456");

        responseHeaderInjector.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response).setHeader("trace-id", "trace-123");
        verify(response).setHeader("span-id", "span-456");
    }

    @Test
    void doFilterInternal_skipsHeadersWhenSpanMissing() throws Exception {
        when(tracer.currentSpan()).thenReturn(null);

        responseHeaderInjector.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setHeader(anyString(), anyString());
    }
}
