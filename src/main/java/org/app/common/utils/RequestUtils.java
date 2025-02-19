package org.app.common.utils;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RequestUtils {

    public static final String REQUEST_ID = "X-Request-ID";

    public static final String USER_ADDRESS = "X-FORWARDED-FOR";

    public static final String TOKEN_HEADER = "Authorization";

    public static final String TOKEN_PREFIX = "Bearer ";

    private RequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    protected Optional<String> getToken(HttpServletRequest request) {
        return Optional.of(request.getHeader(TOKEN_HEADER))
                .filter(token -> StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, ""))
                .or(Optional::empty);
    }

    public static String getRequestId(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader(REQUEST_ID);
    }

    public static String getRemoteAddress(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader(USER_ADDRESS);
    }

    public static String getUrl(HttpServletRequest request) {
        if (request == null) return null;
        return request.getRequestURL().toString();
    }

    public static String getFullUrl(HttpServletRequest request) {
        if (request == null) return null;
        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }

    public static String getRemoteAddress(WebAuthenticationDetails webDetails) {
        return (webDetails != null) ? webDetails.getRemoteAddress() : null;
    }

    public static String getDomain(HttpServletRequest request) {
        String host = request.getHeader("host");
        if (host != null && host.contains(":")) {
            return host.substring(0, host.indexOf(':'));
        }
        return host;
    }

    public static String getRequestHeaders(HttpServletRequest request) {
        if (request == null) return "";
        Map<String, String> map = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map.toString();
    }
}
