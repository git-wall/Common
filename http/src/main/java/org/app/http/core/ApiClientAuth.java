package org.app.common.client.http;

import org.app.common.support.JType;
import org.app.common.utils.JacksonUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class ApiClientAuth {
    private final HttpClient httpClient;
    private final String authUrl;
    private final String tokenFieldName;
    private final Map<String, String> authHeaders;
    private final Map<String, String> authBody;
    private CompletableFuture<String> currentToken;

    public ApiClientAuth(HttpClient httpClient, String authUrl, String tokenFieldName) {
        this.httpClient = httpClient;
        this.authUrl = authUrl;
        this.tokenFieldName = tokenFieldName;
        this.authHeaders = new HashMap<>(8);
        this.authBody = new HashMap<>(3);
    }

    // Add auth header
    public ApiClientAuth addHeader(String key, String value) {
        this.authHeaders.put(key, value);
        return this;
    }

    // Add auth body parameter
    public ApiClientAuth addBodyParameter(String key, String value) {
        this.authBody.put(key, value);
        return this;
    }

    // Add Basic Auth header from username/password
    public ApiClientAuth setBasicAuth(String username, String password) {
        String credentials = java.util.Base64
            .getEncoder()
            .encodeToString(
                (username + ":" + password).getBytes(StandardCharsets.UTF_8)
            );
        this.authHeaders.put("Authorization", "Basic " + credentials);
        return this;
    }

    // Add client credentials to body (OAuth2 client_credentials flow)
    public ApiClientAuth setClientCredentials(String clientId, String clientSecret) {
        this.authBody.put("grant_type", "client_credentials");
        this.authBody.put("client_id", clientId);
        this.authBody.put("client_secret", clientSecret);
        return this;
    }

    // Add username/password to body (OAuth2 password flow)
    public ApiClientAuth setPasswordGrant(String username, String password, String clientId, String clientSecret) {
        this.authBody.put("grant_type", "password");
        this.authBody.put("username", username);
        this.authBody.put("password", password);
        this.authBody.put("client_id", clientId);
        this.authBody.put("client_secret", clientSecret);
        return this;
    }

    // Get current token async, if null then get new token
    public CompletableFuture<String> getTokenAsync(boolean isOld) {
        if (isOld) {
            return requestNewTokenAsync();
        }

        return currentToken;
    }

    // Force get new token async (for 401 cases)
    public CompletableFuture<String> refreshTokenAsync() {
        synchronized (this) {
            currentToken = requestNewTokenAsync();
            return currentToken;
        }
    }

    private CompletableFuture<String> requestNewTokenAsync() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .uri(URI.create(authUrl));

        // Add headers
        authHeaders.forEach(requestBuilder::header);

        // Prepare body as form-encoded
        String requestBody = "";
        if (!authBody.isEmpty() && authBody.size() > 1) {
            requestBody = authBody.entrySet()
                .stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" +
                    URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        }

        HttpRequest request = requestBuilder
            .POST(HttpRequest.BodyPublishers.ofString(requestBody))
            .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenCompose(response -> {
                if (response.statusCode() >= 400) {
                    return CompletableFuture.failedFuture(
                        new RuntimeException("Auth failed: " + response.statusCode() + ", Body: " + response.body())
                    );
                }

                try {
                    // Parse response to get token
                    Map<String, Object> responseData = JacksonUtils.readValue(response.body(), JType.MAP_STRING_OBJECT);

                    Object tokenObj = responseData.get(tokenFieldName);
                    if (tokenObj == null) {
                        return CompletableFuture.failedFuture(
                            new RuntimeException("Token field '" + tokenFieldName + "' not found in response")
                        );
                    }

                    return CompletableFuture.completedFuture(tokenObj.toString());

                } catch (Exception e) {
                    return CompletableFuture.failedFuture(e);
                }
            });
    }
}
