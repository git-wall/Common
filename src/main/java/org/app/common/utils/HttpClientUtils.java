package org.app.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
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

    public static <T> T read(T obj, String url, JavaType javaType) {
        return decode(bodyAsString(requestPost(obj, url)), javaType);
    }

    public static <T> T read(T obj, String url, String token, JavaType javaType) {
        return decode(bodyAsString(requestPost(obj, url, token)), javaType);
    }

    public static <T> HttpResponse<String> call(T obj, String url) {
        return response(requestPost(obj, url));
    }

    public static <T> HttpResponse<String> call(T obj, String url, String token) {
        return response(requestPost(obj, url, token));
    }

    public static <T> CompletableFuture<T> readAsync(T obj, String url, JavaType javaType) {
        return CompletableFuture
                .supplyAsync(() -> requestPost(obj, url))
                .thenApply(HttpClientUtils::bodyAsString)
                .thenApply(e -> HttpClientUtils.decode(e, javaType));
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

    public static <T> HttpRequest requestPost(T obj, String url, String token) {
        try {
            String body = JacksonUtils.mapper().writeValueAsString(obj);

            return HttpRequest.newBuilder(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .headers("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> HttpRequest requestPost(T obj, String url, String token, long seconds) {
        return HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Accept", MediaType.ALL_VALUE)
                .headers("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .headers("Accept", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString(JacksonUtils.toJson(obj)))
                .timeout(Duration.ofSeconds(seconds))
                .build();
    }

    public static void noBody(HttpRequest request) {
        try {
            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void noBody(HttpClient client, HttpRequest request) {
        try {
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void noBodyAsync(HttpRequest request) {
        HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    public static void noBodyAsync(HttpClient client, HttpRequest request) {
        client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    public static String bodyAsString(HttpRequest request) {
        try {
            return HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString())
                    .body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String bodyAsString(HttpClient client, HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse<String> response(HttpRequest request) {
        try {
            return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpResponse<String> response(HttpClient client, HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T decode(String response, JavaType javaType) {
        return JacksonUtils.readValue(response, javaType);
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

    public static HttpClient buildHttp1Client(HttpClient.Redirect policy, long timeout, InetSocketAddress inetSocketAddress) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(policy)
                .connectTimeout(Duration.ofSeconds(timeout))
                .proxy(ProxySelector.of(inetSocketAddress))
                .authenticator(Authenticator.getDefault())
                .build();
    }

    public static HttpClient buildHttp2Client(HttpClient.Redirect policy, long timeout, InetSocketAddress inetSocketAddress) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(policy)
                .connectTimeout(Duration.ofSeconds(timeout))
                .proxy(ProxySelector.of(inetSocketAddress))
                .authenticator(Authenticator.getDefault())
                .build();
    }
}
