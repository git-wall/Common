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

    // auth
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // user remote ip
    public static final String ACCEPT = "accept";
    public static final String USER_AGENT = "user-agent";
    public static final String USER_ADDRESS = "X-FORWARDED-FOR";

    // device id
    public static final String DCM_GU_ID = "x-dcmguid";
    public static final String SUB_NO = " x-up-subno";
    public static final String J_PHONE_UID = "x-jphone-uid";
    public static final String EM_UID = "x-em-uid";

    private RequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    public static String getToken(HttpServletRequest request) {
        var tk = Optional.of(request.getHeader(TOKEN_HEADER))
                .filter(token -> StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, ""))
                .orElse(null);

        if (tk == null)
            return Optional.of(request.getHeader(TOKEN_HEADER.toLowerCase()))
                    .filter(token -> StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX.toLowerCase()))
                    .map(token -> token.replace(TOKEN_PREFIX.toLowerCase(), ""))
                    .orElse(null);

        return null;
    }

    public static String getRequestId(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader(REQUEST_ID);
    }

    public static String getDeviceId(HttpServletRequest request) {
        if (request == null) return null;

        String deviceId = request.getHeader(DCM_GU_ID);

        if (deviceId == null)
            deviceId = request.getHeader(SUB_NO);

        if (deviceId == null)
            deviceId = request.getHeader(J_PHONE_UID);

        if (deviceId == null)
            deviceId = request.getHeader(EM_UID);

        return deviceId;
    }

    public static String getRemoteAddress(HttpServletRequest request) {
        if (request == null) return null;

        String remoteAddress = request.getHeader(USER_ADDRESS);

        if (remoteAddress == null)
            remoteAddress = request.getHeader(ACCEPT);

        if (remoteAddress == null)
            remoteAddress = request.getHeader(USER_AGENT);

        return remoteAddress;
    }

    public static String getUrlNoParams(HttpServletRequest request) {
        if (request == null) return null;

        String url = request.getRequestURL().toString();

        // Optionally, you can also get the query parameters
        String queryString = request.getQueryString();

        // If you want the URL without query parameters, you can remove them
        if (queryString != null) {
            url = url.split("\\?")[0];  // Remove query string if exists
        }

        return url;
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
