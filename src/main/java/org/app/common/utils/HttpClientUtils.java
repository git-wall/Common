package org.app.common.utils;


import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

public class HttpClientUtils {
    public static HttpClient buildHttpClient() {
        return HttpClient.newHttpClient();
    }

    public static HttpClient buildHttpClient(ExecutorService executorService, long timeout) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeout))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(executorService)
                .build();
    }

    public static HttpClient buildHttpClient1(HttpClient.Redirect policy, long timeout, InetSocketAddress inetSocketAddress) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(policy)
                .connectTimeout(Duration.ofSeconds(timeout))
                .proxy(ProxySelector.of(inetSocketAddress))
                .authenticator(Authenticator.getDefault())
                .build();
    }

    public static HttpClient buildHttpClient2(HttpClient.Redirect policy, long timeout, InetSocketAddress inetSocketAddress) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(policy)
                .connectTimeout(Duration.ofSeconds(timeout))
                .proxy(ProxySelector.of(inetSocketAddress))
                .authenticator(Authenticator.getDefault())
                .build();
    }
}
