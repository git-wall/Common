<<<<<<<< HEAD:http/src/main/java/org/app/http/request/RequestUtils.java
package org.app.http.request;
========
package org.app.http.core.request;
>>>>>>>> dev:http/src/main/java/org/app/http/core/request/RequestUtils.java

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.app.jackson.JacksonUtils;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestUtils {

    public static HttpRequest get(String url) {
        return HttpRequest.newBuilder(URI.create(url))
            .GET()
            .build();
    }

    public static HttpRequest get(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        headers.forEach(requestBuilder::header);

        return requestBuilder.uri(URI.create(url)).GET().build();
    }

    public static HttpRequest post(String body, String url) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)
            );

        return requestBuilder
            .uri(URI.create(url))
            .headers("Content-Type", "application/json")
            .build();
    }


    public static HttpRequest post(String body, String url, String tokenValue) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)
            );

        return requestBuilder
            .uri(URI.create(url))
            .header("Authorization", tokenValue)
            .headers("Content-Type", "application/json")
            .build();
    }

    public static HttpRequest post(Object obj, String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(obj == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(JacksonUtils.toJson(obj))
            );

        headers.forEach(requestBuilder::header);

        return requestBuilder.uri(URI.create(url)).build();
    }

    public static HttpRequest put(String url, String body) {
        return HttpRequest.newBuilder(URI.create(url))
            .headers("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(body))
            .build();
    }

    public static HttpRequest put(String body, String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .PUT(body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)
            );
        headers.forEach(requestBuilder::header);

        return requestBuilder.uri(URI.create(url)).build();
    }

    public static HttpRequest delete(String url) {
        return HttpRequest.newBuilder(URI.create(url)).DELETE().build();
    }

    public static HttpRequest delete(String url, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        headers.forEach(requestBuilder::header);
        return requestBuilder.uri(URI.create(url)).DELETE().build();
    }

    public static HttpRequest patch(String url, String body) {
        return HttpRequest.newBuilder(URI.create(url))
            .headers("Content-Type", "application/json")
            .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
            .build();
    }

    public static HttpRequest.Builder getBuilder(String url) {
        return HttpRequest
            .newBuilder(URI.create(url))
            .GET();
    }

    public static HttpRequest.Builder postBuilder(String url, String body, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .POST(body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)
            );

        headers.forEach(requestBuilder::header);

        return requestBuilder.uri(URI.create(url));
    }
    public static HttpRequest.Builder putBuilder(String url, String body, Map<String, String> headers) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
            .PUT(body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body)
            );

        headers.forEach(requestBuilder::header);

        return requestBuilder.uri(URI.create(url));
    }

    public static HttpRequest.Builder deleteBuilder(String url) {
        return HttpRequest.newBuilder(URI.create(url)).DELETE();
    }
}
