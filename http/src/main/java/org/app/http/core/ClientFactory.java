package org.app.http.core;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class ClientFactory {

    public static HttpClient defaultClient() {
        return HttpClient.newBuilder()
            // HTTP/2 if supported Auto downgrade to HTTP/1.1 if not
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(java.time.Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    public HttpClient buildClient(ExecutorService executorService, long second) {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(second))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .executor(executorService)
            .build();
    }
}
