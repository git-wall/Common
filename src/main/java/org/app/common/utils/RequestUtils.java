package org.app.common.utils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Utility class for handling HTTP request-related operations.
 * This class provides methods to extract various information from HTTP requests,
 * such as tokens, request IDs, device IDs, remote addresses, and URLs.
 * {@link org.apache.http.HttpHeaders HttperHeaders} is used for standard HTTP headers.
 */
public class RequestUtils {

    public static final String REQUEST_ID = "X-Request-ID";

    // auth
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
        return Optional.of(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(token -> StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, ""))
                .orElse(null);
    }

    public static Optional<String> getTokenBy(HttpServletRequest request) {
        return Optional.of(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(token -> StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX))
                .map(token -> token.replace(TOKEN_PREFIX, ""))
                .or(Optional::empty);
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

    public static @NotNull HttpRequest rewriteWrapper(HttpRequest request, String baseUrl) {
        if (baseUrl == null) return request;
        return new HttpRequestWrapper(request) {
            @Override
            public @NotNull URI getURI() {
                try {
                    return new URI(baseUrl + super.getURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    public static void authEntryPointHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    public static void accessDeniedHandler(HttpServletRequest request, HttpServletResponse response, Exception ex) {
        response.setStatus(HttpStatus.FORBIDDEN.value());
    }

    public static String getCurl() {
        return toCurl(getHttpServletRequest());
    }

    public static String toCurl(HttpServletRequest request) {
        if (request == null) return "";

        StringBuilder curl = new StringBuilder("curl");

        // Add method
        curl.append(" -X ").append(request.getMethod());

        // Add headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            // Skip common headers that might contain sensitive information
            if (!headerName.equalsIgnoreCase("cookie") &&
                !headerName.equalsIgnoreCase("authorization")) {
                curl.append(" -H '").append(headerName).append(": ").append(headerValue).append("'");
            }
        }

        // Add request parameters if it's a GET request
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String queryString = request.getQueryString();
            if (queryString != null && !queryString.isEmpty()) {
                curl.append(" '").append(getFullURL(request)).append("'");
            } else {
                curl.append(" '").append(request.getRequestURL()).append("'");
            }
            return curl.toString();
        }

        // For POST/PUT/PATCH requests, add the body
        try {
            // Add URL
            curl.append(" '").append(request.getRequestURL()).append("'");

            // Get the request body
            String body = getRequestBody(request);
            if (body != null && !body.isEmpty()) {
                String contentType = request.getContentType();
                if (contentType != null && contentType.contains("application/json")) {
                    curl.append(" -H 'Content-Type: application/json'");
                    curl.append(" -d '").append(body).append("'");
                } else {
                    curl.append(" --data '").append(body).append("'");
                }
            }
        } catch (Exception e) {
            curl.append(" # Error reading request body: ").append(e.getMessage());
        }

        return curl.toString();
    }

    private static String getFullURL(HttpServletRequest request) {
        StringBuilder requestURL = new StringBuilder(request.getRequestURL().toString());
        String queryString = request.getQueryString();
        if (queryString != null) {
            requestURL.append('?').append(queryString);
        }
        return requestURL.toString();
    }

    private static String getRequestBody(HttpServletRequest request) {
        try {
            WrapBodyHttpServletRequest cachedRequest = new WrapBodyHttpServletRequest(request);
            return cachedRequest.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    @Getter
    private static class WrapBodyHttpServletRequest extends HttpServletRequestWrapper {
        private final String body;

        public WrapBodyHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                InputStream inputStream = request.getInputStream();
                if (inputStream != null) {
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    char[] charBuffer = new char[128];
                    int bytesRead;
                    while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                        stringBuilder.append(charBuffer, 0, bytesRead);
                    }
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            body = stringBuilder.toString();
        }

        @Override
        public ServletInputStream getInputStream() {
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
            return new ServletInputStream() {
                @Override
                public boolean isFinished() {
                    return false;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public int read() {
                    return byteArrayInputStream.read();
                }
            };
        }
    }
}
