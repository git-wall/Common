package org.app.common.interceptor;

import brave.Tracer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.app.common.context.TracingContext;
import org.app.common.utils.RequestUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestIdFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {

        try {
            String requestId = resolveRequestId(request);

            TracingContext.putRequestId(requestId);
            MDC.put("requestId", requestId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
            TracingContext.clear();
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String headerId = RequestUtils.getRequestId(request);
        if (StringUtils.hasText(headerId)) {
            return headerId;
        }

        // Existing trace (no create new span)
        var span = tracer.currentSpan();
        if (span != null) {
            return span.context().traceIdString();
        }

        // Fallback
        return UUID.randomUUID().toString();
    }
}
