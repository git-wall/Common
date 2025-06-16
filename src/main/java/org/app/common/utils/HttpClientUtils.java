package org.app.common.utils;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public class HttpClientUtils {

    public static void send(Object request, String url) {
        noBody(requestPost(request, url));
    }

    public static void sendAsync(Object request, String url) {
        noBodyAsync(requestPost(request, url));
    }

    public static <T> T read(T obj, String url) {
        return decode(body(requestPost(obj, url)));
    }

    public static <T> CompletableFuture<T> readAsync(T obj, String url) {
        return CompletableFuture
                .supplyAsync(() -> requestPost(obj, url))
                .thenApply(HttpClientUtils::body)
                .thenApply(HttpClientUtils::decode);
    }

    public static HttpRequest requestGet(String url, String API_KEY) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Key", API_KEY)
                .headers("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build();
    }

    public static HttpRequest requestGet(String url) {
        return HttpRequest.newBuilder(URI.create(url))
                .headers("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
                .GET()
                .build();
    }

    public static <T> HttpRequest requestPost(T obj, String url) {
        try {
            String body = JacksonUtils.mapper().writeValueAsString(obj);

            return HttpRequest.newBuilder(URI.create(url))
                    .headers("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void noBody(HttpRequest request) {
        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void noBodyAsync(HttpRequest request) {
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    public static String body(HttpRequest request) {
        try {
            return HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T decode(String response) {
        return JacksonUtils.readValue(response);
    }

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
