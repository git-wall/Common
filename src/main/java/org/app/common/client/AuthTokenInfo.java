package org.app.common.client;

import org.app.common.client.rest.HttpHeaderUtils;
import org.app.common.support.Type;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Interface for providing authentication token information
 * Used for internal API calls that require authentication
 */
public interface AuthTokenInfo {
    /**
     * Get the URI for the authentication endpoint
     *
     * @return URI string
     */
    String getURI();

    /**
     * Get the current authentication token
     *
     * @return Token string
     */
    String getToken();

    /**
     * Retrieves the HTTP method to be used for the authentication request.
     *
     * @return The HTTP method as an instance of {@link HttpMethod}.
     */
    HttpMethod getHttpMethod();

    /**
     * Provides the default request body for the authentication request.
     * The body contains the grant type required for client credentials authentication.
     *
     * @return A map containing the request body parameters.
     */
    default Map<String, String> getBody() {
        return Map.of("grant_type", "client_credentials");
    }

    /**
     * Specifies the default timeout for the authentication request.
     *
     * @return The timeout value in milliseconds.
     */
    default int timeout() {
        return 5000; // Default timeout in milliseconds
    }

    /**
     * Refreshes the authentication token by calling the authentication server
     *
     * @return The new token
     */
    default String refreshToken() {
        var header = HttpHeaderUtils.createHeaders(getToken());
        var entity = new HttpEntity<>(getBody(), header);
        var restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                getURI(), getHttpMethod(), entity, Type.paramRefer()
        );

        var body = response.getBody();
        if (body == null || body.isEmpty()) return null;

        return body.entrySet()
                .stream()
                .filter(e -> {
                    String key = e.getKey().toLowerCase();
                    return key.contains("access") && key.contains("token");
                })
                .map(e -> e.getValue().toString())
                .findFirst()
                .orElse(null);
    }
}
