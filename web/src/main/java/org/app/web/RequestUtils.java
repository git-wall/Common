package org.app.web;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utility class for handling HTTP request-related operations.
 * This class provides methods to extract various information from HTTP requests,
 * such as tokens, request IDs, device IDs, remote addresses, and URLs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class RequestUtils {
    public static final String REQUEST_ID = "X-Request-Id";

    // auth
    public static final String TOKEN_PREFIX = "Bearer ";

    public static final String DEVICE_ID = "device-id";
    public static final String DCM_GU_ID = "x-dcmguid";
    public static final String SUB_NO = " x-up-subno";
    public static final String J_PHONE_UID = "x-jphone-uid";
    public static final String EM_UID = "x-em-uid";
    public static final String EM_GUID = "x-em-guid";
    public static final String EM_NAME = "x-em-name";
    public static final String EM_EMAIL = "x-em-email";
    public static final String X_APP_VERSION = "X-App-Version";

    public static final String USER_AGENT = "User-Agent";
    public static final String USER_ID = "x-user-id";
    public static final String USER_NAME = "x-user-name";
    public static final String USER_EMAIL = "x-user-email";

    public static final String SERVICE_ID = "X-Service-Id";
    public static final String TENANT_ID = "X-Tenant-Id";

    public static final String HOST = "Host";

    // user remote ip
    /**
     * <pre>{@code
     * Header                   | IN
     * ----------------------------------------------
     * X-Forwarded-For          | Nginx, HAProxy, ALB
     * X-Real-IP                | Nginx
     * Forwarded                | RFC 7239
     * CF-Connecting-IP         | Cloudflare
     * True-Client-IP           | Akamai
     * }
     * </pre>
     * */
    private static final String[] IP_HEADER_CANDIDATES = {
        "X-Real-IP",
        "X-Client-IP",
        "X-Forwarded",
        "Forwarded-For",
        "Forwarded",
        "CF-Connecting-IP",
        "True-Client-IP",
        "X-FORWARDED-FOR",
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

    private static final Set<String> SENSITIVE_HEADERS = Set.of(
        "authorization",
        "cookie",
        "set-cookie",
        "x-api-key",
        "x-csrf-token"
    );

    private static final Set<String> SENSITIVE_PARAMS = Set.of(
        "token",
        "access_token",
        "refresh_token",
        "password",
        "secret",
        "otp",
        "code",
        "auth",
        "signature"
    );

    public static HttpServletRequest getRequest() {
        return RequestContextAccessor.getRequest();
    }

    public static String getTenantId(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        return request.getHeader(TENANT_ID);
    }

    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        return request.getHeader(USER_AGENT);
    }

    public static String getToken(HttpServletRequest request) {
        return Optional.of(request.getHeader("Authorization"))
            .filter(token -> token.startsWith(TOKEN_PREFIX))
            .map(token -> token.replace(TOKEN_PREFIX, ""))
            .orElse(null);
    }

    public static Optional<String> getTokenBy(HttpServletRequest request) {
        return Optional.of(request.getHeader("Authorization"))
            .filter(token -> token.startsWith(TOKEN_PREFIX))
            .map(token -> token.replace(TOKEN_PREFIX, ""))
            .or(Optional::empty);
    }

    public static String getRequestIdOrElse(HttpServletRequest request, Supplier<String> another) {
        return Optional.ofNullable(request)
            .map(RequestUtils::getRequestId)
            .orElse(another.get());
    }

    public static String getRequestId(HttpServletRequest request) {
        if (request == null) return null;
        return request.getHeader(REQUEST_ID);
    }

    public static String getDeviceId(HttpServletRequest request) {
        if (request == null) return null;

        return Stream.of(DCM_GU_ID, SUB_NO, J_PHONE_UID, EM_UID, DEVICE_ID)
            .map(request::getHeader)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
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

    public static String getHost(HttpServletRequest request) {
        if (request == null) return "";
        return request.getHeader(HOST);
    }

    public static String getDomain(HttpServletRequest request) {
        if (request == null) return "";
        String host = request.getHeader(HOST);
        return getDomain(host);
    }

    public static String getDomain(String host) {
        if (host != null && host.contains(":")) {
            return host.substring(0, host.indexOf(':'));
        }
        return host;
    }

    public static String getHeader(HttpServletRequest request, String name) {
        if (request == null || name == null || name.isEmpty()) return "";
        return request.getHeader(name);
    }

    public static String headersAsString(HttpServletRequest request) {
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

    public static String getCurl(HttpServletRequest request) {
        if (request == null) return "";

        StringBuilder curl = new StringBuilder("curl");
        curl.append(" -X ").append(request.getMethod());

        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();

            if (isSensitiveHeader(name)) continue;

            curl.append(" -H '")
                .append(name)
                .append(": ")
                .append(mask(request.getHeader(name)))
                .append("'");
        }

        curl.append(" '").append(getFullUriWithoutSensitiveParams(request)).append("'");

        return curl.toString();
    }

    static boolean isSensitiveHeader(String name) {
        return SENSITIVE_HEADERS.contains(name.toLowerCase());
    }

    public static String getFullUriWithoutSensitiveParams(HttpServletRequest request) {
        StringBuilder uri = new StringBuilder(request.getRequestURL());

        String query = request.getQueryString();
        if (query == null || query.isBlank()) {
            return uri.toString();
        }

        uri.append("?");

        String[] params = query.split("&");
        boolean first = true;

        for (String param : params) {
            String[] kv = param.split("=", 2);
            String key = kv[0];
            String value = kv.length > 1 ? kv[1] : "";

            if (!first) uri.append("&");
            first = false;

            if (isSensitiveParam(key)) {
                uri.append(key).append("=***");
            } else {
                uri.append(key).append("=").append(mask(value));
            }
        }

        return uri.toString();
    }

    private static boolean isSensitiveParam(String key) {
        return SENSITIVE_PARAMS.contains(key.toLowerCase());
    }

    public static String mask(String value) {
        if (value == null || value.isBlank()) return "";

        int len = value.length();

        // JWT / long token
        if (len > 32) {
            return value.substring(0, 6)
                + "****"
                + value.substring(len - 4);
        }

        // Medium
        if (len > 12) {
            return value.substring(0, 3)
                + "***"
                + value.substring(len - 3);
        }

        // Short
        if (len > 4) {
            return value.charAt(0) + "**" + value.charAt(len - 1);
        }

        return "***";
    }

    public static String getFullUri(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append('?').append(queryString);
        }
        return requestURL.toString();
    }

    public static boolean isServiceAllowed(HttpServletRequest request, List<String> allowedServiceIds) {
        if (request == null || allowedServiceIds == null || allowedServiceIds.isEmpty())
            return false;

        String serviceId = request.getHeader(SERVICE_ID);
        return serviceId != null && allowedServiceIds.contains(serviceId);
    }
}
