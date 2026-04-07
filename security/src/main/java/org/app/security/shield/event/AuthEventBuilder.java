package org.app.security.shield.event;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.observation.context.ContextKey;
import org.app.security.shield.constant.AuthEventType;
import org.app.security.shield.constant.AuthFailureReason;
import org.app.web.RequestUtils;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthEventBuilder {

    public static AuthSecurityEvent success(JwtAuthenticationToken auth, HttpServletRequest req, String serviceName) {
        AuthSecurityEvent e = base(req, serviceName);
        e.eventType = AuthEventType.AUTH_SUCCESS;
        e.subject = auth.getToken().getSubject();
        return e;
    }

    public static AuthSecurityEvent failure(Exception ex, HttpServletRequest req, String serviceName) {
        AuthFailureReason reason = AuthFailureReasonMapper.map(ex);
        AuthSecurityEvent e = base(req, serviceName);
        e.eventType = AuthEventType.AUTH_FAILED;
        e.failureReason = reason;
        e.subject = null;
        return e;
    }

    private static AuthSecurityEvent base(HttpServletRequest req, String serviceName) {
        AuthSecurityEvent e = new AuthSecurityEvent();
        e.service = serviceName;
        e.path = req.getRequestURI();
        e.method = req.getMethod();
        e.ip = req.getRemoteAddr();
        e.userAgent = RequestUtils.getUserAgent(req);
        e.traceId = org.slf4j.MDC.get(ContextKey.TRACE_ID);
        e.timestamp = Instant.now();
        return e;
    }
}
