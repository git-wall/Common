package org.app.common.interceptor.rest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.TracingContext;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class LogRequestInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Log request details (optional)
        startCollection(request, body);

        // Execute the request
        ClientHttpResponse response = execution.execute(request, body);

        // Log response details (optional)
        endCollection(response);

        return response;
    }

    private void startCollection(HttpRequest request, byte[] body) {
        String requestId = TracingContext.getRequestId();
        log.info(
                "{} - URI: {}, Method: {}, Body: {}",
                requestId,
                request.getURI(),
                request.getMethod(),
                new String(body, StandardCharsets.UTF_8)
        );
    }

    @SneakyThrows
    private void endCollection(ClientHttpResponse response) {
        String requestId = TracingContext.getRequestId();
        log.info("{} - Response Body: {}", requestId, StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8));
    }
}
