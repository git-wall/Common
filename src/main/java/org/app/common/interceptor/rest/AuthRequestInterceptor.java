package org.app.common.interceptor.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;

@RequiredArgsConstructor
public class AuthRequestInterceptor implements ClientHttpRequestInterceptor {

    private final String authToken;

    @NonNull
    @Override
    public ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        // Add the Authorization header if token is present
        if (authToken != null && !authToken.isEmpty()) {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
        }
        return execution.execute(request, body);
    }
}
