package org.app.eav.utils;

import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class RequestUtils {

    public static final String REQUEST_ID = "X-Request-ID";

    // auth
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    // device id
    public static final String DCM_GU_ID = "x-dcmguid";
    public static final String SUB_NO = " x-up-subno";
    public static final String J_PHONE_UID = "x-jphone-uid";
    public static final String EM_UID = "x-em-uid";

    // user remote ip
    private static final String[] IP_HEADER_CANDIDATES = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    private RequestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    public static String getToken(HttpServletRequest request) {
        return Optional.of(request.getHeader(TOKEN_HEADER))
                .filter(token -> StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, ""))
                .orElse(null);
    }

    public static String getToken() {
        return getToken(Objects.requireNonNull(getHttpServletRequest()));
    }

    public static String getRequestId(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader(REQUEST_ID);
    }

    public static String getRequestId() {
        return getRequestId(getHttpServletRequest());
    }

    public static String getDeviceId() {
        return getDeviceId(getHttpServletRequest());
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

    public static String getRemoteAddress() {
        return getRemoteAddress(getHttpServletRequest());
    }

    public static String getRemoteAddress(HttpServletRequest request) {
        if (request == null) return null;
        // Check each header in the IP_HEADER_CANDIDATES
        for (String header : IP_HEADER_CANDIDATES) {
            String remoteAddress = request.getHeader(header);
            if (remoteAddress != null && !remoteAddress.isEmpty()) {
                return remoteAddress.split(",")[0].trim(); // Return the first IP if multiple are present
            }
        }
        // Fallback to getRemoteAddr if no headers found
        return request.getRemoteAddr();
    }

    public static String getUrlNoParams() {
        return getUrlNoParams(getHttpServletRequest());
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

    public static String getUrl() {
        return getUrl(getHttpServletRequest());
    }

    public static String getUrl(HttpServletRequest request) {
        if (request == null) return null;

        return request.getRequestURL().toString();
    }

    public static String getFullUrl() {
        return getFullUrl(getHttpServletRequest());
    }

    public static String getFullUrl(HttpServletRequest request) {
        if (request == null) return null;
        return request.getRequestURL().toString() + "?" + request.getQueryString();
    }

//    public static String getRemoteAddress(WebAuthenticationDetails webDetails) {
//        return (webDetails != null) ? webDetails.getRemoteAddress() : null;
//    }

    public static String getDomain() {
        return getDomain(getHttpServletRequest());
    }

    public static String getDomain(HttpServletRequest request) {
        String host = request.getHeader("host");
        if (host != null && host.contains(":")) {
            return host.substring(0, host.indexOf(':'));
        }
        return host;
    }

    public static String getRequestHeaders() {
        return getRequestHeaders(getHttpServletRequest());
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
