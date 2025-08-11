package org.app.common.client.rest.interceptor;

import lombok.RequiredArgsConstructor;
import org.app.common.client.rest.HeaderUtils;
import org.app.common.utils.RequestUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class AuthRequestInterceptor implements ClientHttpRequestInterceptor {

    private String baseUrl;

    private String token;

    private Supplier<String> refreshTokenSupplier;

    public AuthRequestInterceptor(String baseUrl, String token, Supplier<String> refreshTokenSupplier) {
        this.baseUrl = baseUrl;
        this.token = StringUtils.hasText(token) ? token : refreshTokenSupplier.get();
        this.refreshTokenSupplier = refreshTokenSupplier;
    }

    @NonNull
    @Override
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        // Add the Authorization header if the token is present
        if (StringUtils.hasText(token)) {
            request.getHeaders().set(HttpHeaders.AUTHORIZATION, HeaderUtils.token(token));
        }

        var response = execution.execute(RequestUtils.rewriteWrapper(request, baseUrl), body);

        // If we get an UNAUTHORIZED response and haven't yet tried refreshing the token
        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            // Remove the old authorization header
            request.getHeaders().remove(HttpHeaders.AUTHORIZATION);

            // Get a fresh token
            token = refreshTokenSupplier.get();

            // Add the new token to the request
            if (StringUtils.hasText(token)) {
                request.getHeaders().set(HttpHeaders.AUTHORIZATION, HeaderUtils.token(token));
                // Retry the request with the new token
                response = execution.execute(RequestUtils.rewriteWrapper(request, baseUrl), body);
            }
        }

        return response;
    }
}
