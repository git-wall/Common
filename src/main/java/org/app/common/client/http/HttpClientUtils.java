package org.app.common.client.http;

import com.fasterxml.jackson.databind.JavaType;
import lombok.SneakyThrows;
import org.app.common.client.http.request.HttpRequestUtils;
import org.app.common.client.http.response.HttpResponseUtils;
import org.app.common.utils.JacksonUtils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class HttpClientUtils {

    private HttpClientUtils() {
        // Utility class, no instantiation
    }

    public static <T> T read(Object obj, String url, JavaType javaType) {
        return decode(bodyAsString(HttpRequestUtils.post(JacksonUtils.toJson(obj), url)), javaType);
    }

    public static <T> T read(T obj, String url, String token, JavaType javaType) {
        return decode(bodyAsString(HttpRequestUtils.post(JacksonUtils.toJson(obj), url, token)), javaType);
    }

    public static HttpResponse<String> call(Object obj, String url) {
        return response(HttpRequestUtils.post(JacksonUtils.toJson(obj), url));
    }

    public static HttpResponse<String> call(Object obj, String url, String token) {
        return response(HttpRequestUtils.post(JacksonUtils.toJson(obj), url, token));
    }

    @SneakyThrows
    public static String bodyAsString(HttpRequest request) {
        return bodyAsString(HttpClient.newHttpClient(), request);
    }

    @SneakyThrows
    public static String bodyAsString(HttpClient client, HttpRequest request) {
        return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    @SneakyThrows
    public static HttpResponse<String> response(HttpRequest request) {
        return response(HttpClient.newHttpClient(), request);
    }

    @SneakyThrows
    public static HttpResponse<String> response(HttpClient client, HttpRequest request) {
        return response(client, request, HttpResponse.BodyHandlers.ofString());
    }

    @SneakyThrows
    public static <T> HttpResponse<T> response(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        return client.send(request, responseBodyHandler);
    }

    private static <T> T decode(String response, JavaType javaType) {
        return JacksonUtils.readValue(response, javaType);
    }

    public static class Auth {

        private Auth() {
            // Utility class, no instantiation
        }

        public static Authenticator basic(String username, String password) {
            return new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            };
        }
    }

    public static class Async {

        private Async() {
            // Utility class, no instantiation
        }

        public static void send(HttpClient client, Object request, String url) {
            nobody(client, HttpRequestUtils.post(JacksonUtils.toJson(request), url));
        }

        private static void nobody(HttpClient client, HttpRequest request) {
            client.sendAsync(request, HttpResponse.BodyHandlers.discarding());
        }

        public static <T> T read(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return client.sendAsync(request, responseBodyHandler).join().body();
        }

        public static <T> T read(HttpClient client, HttpRequest request, JavaType javaType) {
            return client
                .<T>sendAsync(request, HttpResponseUtils.generic(javaType))
                .join()
                .body();
        }

        public static <T> HttpResponse<T> call(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return client.sendAsync(request, responseBodyHandler).join();
        }

        public static <T> HttpResponse<T> call(HttpClient client, HttpRequest request, JavaType javaType) {
            return client.<T>sendAsync(request, HttpResponseUtils.generic(javaType)).join();
        }
    }
}


