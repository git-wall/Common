package org.app.common.client.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.app.common.utils.JacksonUtils;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class HttpUtils {

    public static <T> T read(T obj, String url) {
        return decode(body(request(obj, url)));
    }

    public static <T> CompletableFuture<T> readAsync(T obj, String url) {
        return CompletableFuture
                .supplyAsync(() -> request(obj, url))
                .thenApply(HttpUtils::body)
                .thenApply(HttpUtils::decode);
    }

    public static <T> HttpRequest request(T obj, String url) {
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
}
