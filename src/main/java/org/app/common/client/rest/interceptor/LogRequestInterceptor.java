package org.app.common.client.rest.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.app.common.context.TracingContext;
import org.app.common.utils.RequestUtils;
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
        String requestId = TracingContext.getRequestId();
        request.getHeaders().add(RequestUtils.REQUEST_ID, requestId);

        String requestInfo = String.format(
            "Request{URI: %s, Method: %s, Body: %s} ",
            request.getURI(),
            request.getMethod(),
            new String(body, StandardCharsets.UTF_8)
        );

        long startTime = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long endTime = System.currentTimeMillis();

        String responseInfo = String.format(
            "Response{Status: %s, Body: %s}",
            response.getStatusCode(),
            StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8)
        );

        log.info("{} - Duration:{} - {} - {}", requestId, (endTime - startTime), requestInfo, responseInfo);

        return response;
    }
}
