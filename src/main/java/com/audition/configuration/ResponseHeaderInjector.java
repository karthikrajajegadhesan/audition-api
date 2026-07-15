package com.audition.configuration;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Getter
public class ResponseHeaderInjector extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "trace-id";
    private static final String SPAN_ID_HEADER = "span-id";

    private final Tracer tracer;

    public ResponseHeaderInjector(final Tracer tracer) {
        super();
        this.tracer = tracer;
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
        final HttpServletResponse response, final FilterChain filterChain)
        throws ServletException, IOException {
        final Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            response.setHeader(TRACE_ID_HEADER, currentSpan.context().traceId());
            response.setHeader(SPAN_ID_HEADER, currentSpan.context().spanId());
        }
        filterChain.doFilter(request, response);
    }

}
