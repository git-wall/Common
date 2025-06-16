# REST Client Library with Token Refresh

This library provides utilities for making REST API calls with automatic token refresh capabilities. It's designed to be easy to use and flexible for different authentication scenarios.

## Key Components

### Interfaces

- **AuthTokenInfo**: Provides information about authentication tokens, including methods for token refresh.
- **ClientInfo**: Provides information about the client making the API calls.

### Utilities

- **RestTemplateUtils**: Utilities for creating RestTemplate instances with various configurations.
- **RestfulApi**: A fluent API for making REST calls with a builder pattern.
- **InterceptorUtils**: Utilities for creating HTTP request interceptors.
- **AuthRequestInterceptor**: An interceptor that handles authentication and token refresh.

## Token Refresh Functionality

The library supports automatic token refresh when API calls receive a 401 Unauthorized response. This is handled by the AuthRequestInterceptor, which will:

1. Detect a 401 Unauthorized response
2. Remove the old authorization header
3. Get a fresh token using the provided token supplier
4. Retry the request with the new token

## Usage Examples

### Basic Usage with Token Refresh

```java
// Create AuthTokenInfo and ClientInfo
AuthTokenInfo authTokenInfo = new YourAuthTokenInfoImpl();
ClientInfo clientInfo = new YourClientInfoImpl();

// Create a RestTemplate with token refresh capability
RestTemplate restTemplate = RestTemplateUtils.buildWithTokenRefresh(
        authTokenInfo,
        clientInfo
);

// Use the RestTemplate for API calls
// The token will be automatically refreshed if needed
String response = restTemplate.getForObject("https://api.example.com/resource", String.class);
```

### Using the Fluent API

```java
// Create AuthTokenInfo and ClientInfo
AuthTokenInfo authTokenInfo = new YourAuthTokenInfoImpl();
ClientInfo clientInfo = new YourClientInfoImpl();

// Create a RestfulApi instance with token refresh capability
RestfulApi<YourResponseType> api = RestfulApi.ofAuthWithRefresh(authTokenInfo, clientInfo);

// Configure and execute the request
YourResponseType response = api
        .url("https://api.example.com/resource")
        .method(HttpMethod.GET)
        .exchange()
        .get();

// The token will be automatically refreshed if needed during the exchange
```

## Implementing the Interfaces

### AuthTokenInfo Implementation

```java
public class YourAuthTokenInfoImpl implements AuthTokenInfo {
    private String token;
    
    @Override
    public String getBaseUrl() {
        return "https://api.example.com";
    }
    
    @Override
    public String getHostname() {
        return "api.example.com";
    }
    
    @Override
    public int getPort() {
        return 443;
    }
    
    @Override
    public String getURI() {
        return "/auth/token";
    }
    
    @Override
    public String getToken() {
        return token;
    }
    
    @Override
    public String refreshToken() {
        // Call your authentication service to get a new token
        token = callAuthService();
        return token;
    }
    
    @Override
    public boolean isTokenValid() {
        // Check if the token is still valid
        return !isTokenExpired(token);
    }
    
    private String callAuthService() {
        // Implementation to call your auth service
        return "new-token";
    }
    
    private boolean isTokenExpired(String token) {
        // Implementation to check if token is expired
        return false;
    }
}
```

### ClientInfo Implementation

```java
public class YourClientInfoImpl implements ClientInfo {
    @Override
    public String getHostname() {
        return "api.example.com";
    }
    
    @Override
    public Integer getPort() {
        return 443;
    }
    
    @Override
    public String getUsername() {
        return "username";
    }
    
    @Override
    public String getPassword() {
        return "password";
    }
    
    @Override
    public boolean isBufferRequestBody() {
        return true;
    }
}
```

## Advanced Usage

For more advanced usage, see the `TokenRefreshExample` class in the `org.app.common.client.rest.example` package.

## Best Practices

1. Implement the `refreshToken()` method in your `AuthTokenInfo` implementation to properly obtain a new token from your authentication service.
2. Consider implementing the `isTokenValid()` method to check if the token is still valid before making API calls.
3. Use the `buildWithTokenRefresh()` method in `RestTemplateUtils` to create a RestTemplate with token refresh capability.
4. For complex scenarios, you can create a custom token supplier and use the `customAuthInterceptor()` method in `InterceptorUtils`.