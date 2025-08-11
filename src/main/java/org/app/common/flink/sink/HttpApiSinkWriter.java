package org.app.common.flink.sink;

import org.apache.flink.api.connector.sink2.SinkWriter;
import org.app.common.notification.NotificationInfo;
import org.app.common.support.SnapShot;
import org.app.common.utils.HttpClientUtils;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpApiSinkWriter<T> implements SinkWriter<T> {

    private final String url;
    private final String token;
    private final NotificationInfo notificationInfo;
    private final HttpClient httpClient;

    public HttpApiSinkWriter(String url, String token, NotificationInfo notificationInfo) {
        this.url = url;
        this.token = token;
        this.notificationInfo = notificationInfo;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    @Override
    public void write(T request, Context context) throws IOException, InterruptedException {
        if (request == null) return;
        try {
            HttpRequest httpRequest = HttpClientUtils.requestPost(request, url, token, 30);
            httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            SnapShot.exceptionToLine(e, "Request: " + request, notificationInfo);
        }
    }

    @Override
    public void flush(boolean endOfInput) {
        // No-op: nothing to flush in HTTP fire-and-forget
    }

    @Override
    public void close() {
        // No resources to close
    }
}
