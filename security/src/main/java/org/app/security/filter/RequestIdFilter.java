package org.app.security.filter;

import lombok.RequiredArgsConstructor;
import org.app.observation.context.ContextKey;
import org.app.security.utils.RequestUtils;
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

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String requestId = resolveRequestId(request);

            MDC.put(ContextKey.REQUEST_ID, requestId);

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(ContextKey.REQUEST_ID);
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String headerId = RequestUtils.getRequestId(request);
        if (StringUtils.hasText(headerId)) {
            return headerId;
        }

        // Fallback
        return UUID.randomUUID().toString();
    }
}
