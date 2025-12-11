package org.app.common.client.http.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.app.common.client.http.HttpInterceptor;
import org.app.common.client.http.request.MarkerRequest;
import org.app.common.context.TracingContext;
import org.springframework.stereotype.Component;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class LoggingInterceptor implements HttpInterceptor {

    @Override
    public <T> HttpResponse<T> intercept(MarkerRequest wrapper, Chain chain, HttpClient client) {
        String requestId = TracingContext.getRequestId();

        long startTime = System.currentTimeMillis();
        HttpResponse<T> response = chain.proceed(wrapper);
        long endTime = System.currentTimeMillis();

        String responseInfo = String.format("Response{Status: %s, Body: %s}", response.statusCode(), response.body());

        log.info("{} - Duration:{} - {} - {}", requestId, (endTime - startTime), wrapper.requestInfo(), responseInfo);

        return response;
    }
}
