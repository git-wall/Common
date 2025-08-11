package org.app.common.client.http;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * <pre>{@code
 *     HttpClient httpClient = HttpClient.newHttpClient();
 *
 *         // Case 1: OAuth2 Client Credentials (credentials in body)
 *         ApiClientAuth oauthClient = new ApiClientAuth(httpClient, "https://api.example.com/oauth/token", "access_token")
 *             .setClientCredentials("your_client_id", "your_client_secret");
 *
 *         // Case 2: Basic Auth (username/password in header)
 *         ApiClientAuth basicAuthClient = new ApiClientAuth(httpClient, "https://api.example.com/auth/token", "token")
 *             .setBasicAuth("username", "password");
 *
 *         // Case 3: OAuth2 Password Grant (all in body)
 *         ApiClientAuth passwordGrantClient = new ApiClientAuth(httpClient, "https://api.example.com/oauth/token", "access_token")
 *             .setPasswordGrant("user@example.com", "password123", "client_id", "client_secret");
 *
 *         // Case 4: Custom API (manual setup)
 *         ApiClientAuth customClient = new ApiClientAuth(httpClient, "https://api.example.com/login", "auth_token")
 *             .addHeader("X-API-Key", "your_api_key")
 *             .addBodyParameter("username", "user")
 *             .addBodyParameter("password", "pass")
 *             .addBodyParameter("grant_type", "custom");
 *
 *         // Case 5: API Key in header, credentials in body
 *         ApiClientAuth mixedClient = new ApiClientAuth(httpClient, "https://api.example.com/authenticate", "session_token")
 *             .addHeader("X-Client-ID", "client123")
 *             .setBasicAuth("api_user", "api_secret");
 *
 *         // Use any of them
 *         ApiClient apiClient = ApiClient.from(httpClient, oauthClient);
 *
 *         // Async calls
 *         apiClient.getAsync("https://api.example.com/users")
 *             .thenCompose(response -> {
 *                 System.out.println("Users: " + response.body());
 *                 return apiClient.postAsync("https://api.example.com/users",
 *                     "{\"name\":\"John\",\"email\":\"john@example.com\"}");
 *             })
 *             .thenAccept(response -> {
 *                 System.out.println("Created user: " + response.body());
 *             })
 *             .exceptionally(throwable -> {
 *                 System.err.println("Error: " + throwable.getMessage());
 *                 return null;
 *             });
 *
 *         // Parallel calls
 *         CompletableFuture<String> users = apiClient.getAsync("https://api.example.com/users")
 *             .thenApply(HttpResponse::body);
 *
 *         CompletableFuture<String> orders = apiClient.getAsync("https://api.example.com/orders")
 *             .thenApply(HttpResponse::body);
 *
 *         CompletableFuture.allOf(users, orders)
 *             .thenRun(() -> {
 *                 try {
 *                     System.out.println("Users: " + users.get());
 *                     System.out.println("Orders: " + orders.get());
 *                 } catch (Exception e) {
 *                     e.printStackTrace();
 *                 }
 *             });
 *
 *         // Sync calls still available
 *         try {
 *             var syncResponse = apiClient.get("https://api.example.com/profile");
 *             System.out.println("Sync call: " + syncResponse.body());
 *         } catch (Exception e) {
 *             e.printStackTrace();
 *         }
 * }</pre>
 */
public class ApiClient {
    private final HttpClient httpClient;
    private final ApiClientAuth authClient;

    public ApiClient(HttpClient httpClient, ApiClientAuth authClient) {
        this.httpClient = httpClient;
        this.authClient = authClient;
    }

    // Constructor for non-auth usage
    public ApiClient(HttpClient httpClient) {
        this.httpClient = httpClient;
        this.authClient = null;
    }

    public static ApiClient from(HttpClient httpClient, ApiClientAuth authClient) {
        return new ApiClient(httpClient, authClient);
    }

    public CompletableFuture<HttpResponse<String>> getAsync(String url) {
        return getAsync(url, new HashMap<>());
    }

    public CompletableFuture<HttpResponse<String>> getAsync(String url, Map<String, String> headers) {
        return sendRequestAsync("GET", url, null, headers);
    }

    public CompletableFuture<HttpResponse<String>> postAsync(String url, String body) {
        return postAsync(url, body, new HashMap<>());
    }

    public CompletableFuture<HttpResponse<String>> postAsync(String url, String body, Map<String, String> headers) {
        return sendRequestAsync("POST", url, body, headers);
    }

    public CompletableFuture<HttpResponse<String>> putAsync(String url, String body) {
        return putAsync(url, body, new HashMap<>());
    }

    public CompletableFuture<HttpResponse<String>> putAsync(String url, String body, Map<String, String> headers) {
        return sendRequestAsync("PUT", url, body, headers);
    }

    public CompletableFuture<HttpResponse<String>> deleteAsync(String url) {
        return deleteAsync(url, new HashMap<>());
    }

    public CompletableFuture<HttpResponse<String>> deleteAsync(String url, Map<String, String> headers) {
        return sendRequestAsync("DELETE", url, null, headers);
    }

    private CompletableFuture<HttpResponse<String>> sendRequestAsync(String method, String url, String body, Map<String, String> headers) {
        return executeRequestAsync(method, url, body, headers)
            .thenCompose(response -> {
                // If 401 and we have an auth client, refresh token and retry
                if (response.statusCode() == 401 && authClient != null) {
                    return authClient
                        .refreshTokenAsync()
                        .thenCompose(newToken -> executeRequestAsync(method, url, body, headers));
                }
                return CompletableFuture.completedFuture(response);
            });
    }

    private CompletableFuture<HttpResponse<String>> executeRequestAsync(String method, String url, String body, Map<String, String> headers) {

        CompletableFuture<HttpRequest> requestFuture;

        if (authClient != null) {
            // Get token first, then build request
            requestFuture = authClient
                .getTokenAsync(false)
                .thenApply(token -> buildRequest(method, url, body, headers, token));
        } else {
            // Build request without a token
            requestFuture = CompletableFuture.completedFuture(buildRequest(method, url, body, headers, null));
        }

        return requestFuture
            .thenCompose(request -> httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()));
    }

    private HttpRequest buildRequest(String method, String url, String body, Map<String, String> headers, String token) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(url));

        // Add custom headers
        headers.forEach(requestBuilder::header);

        // Add auth header if token available
        if (token != null) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        // Set method and body
        switch (method.toUpperCase()) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST":
                requestBuilder.POST(body != null ?
                    HttpRequest.BodyPublishers.ofString(body) :
                    HttpRequest.BodyPublishers.noBody());
                break;
            case "PUT":
                requestBuilder.PUT(body != null ?
                    HttpRequest.BodyPublishers.ofString(body) :
                    HttpRequest.BodyPublishers.noBody());
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            default:
                throw new UnsupportedOperationException("Method " + method + " not supported");
        }

        return requestBuilder.build();
    }

    // Utility method to convert async to sync if needed
    public HttpResponse<String> get(String url) {
        return getAsync(url).join();
    }

    public HttpResponse<String> post(String url, String body) {
        return postAsync(url, body).join();
    }

    public HttpResponse<String> put(String url, String body) {
        return putAsync(url, body).join();
    }

    public HttpResponse<String> delete(String url) {
        return deleteAsync(url).join();
    }
}

