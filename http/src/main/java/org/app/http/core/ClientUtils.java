package org.app.http.core;

import com.fasterxml.jackson.databind.JavaType;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.app.http.core.request.RequestUtils;
import org.app.http.core.response.ResponseUtils;
import org.app.jackson.JacksonUtils;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ClientUtils {
    public static <T> T read(Object obj, String url, JavaType javaType) {
        return decode(bodyAsString(RequestUtils.post(JacksonUtils.toJson(obj), url)), javaType);
    }

    public static <T> T read(T obj, String url, String token, JavaType javaType) {
        return decode(bodyAsString(RequestUtils.post(JacksonUtils.toJson(obj), url, token)), javaType);
    }

    public static HttpResponse<String> call(Object obj, String url) {
        return call(RequestUtils.post(JacksonUtils.toJson(obj), url));
    }

    public static HttpResponse<String> call(Object obj, String url, String token) {
        return call(RequestUtils.post(JacksonUtils.toJson(obj), url, token));
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
    public static HttpResponse<String> call(HttpRequest request) {
        return call(HttpClient.newHttpClient(), request);
    }

    @SneakyThrows
    public static HttpResponse<String> call(HttpClient client, HttpRequest request) {
        return call(client, request, HttpResponse.BodyHandlers.ofString());
    }

    @SneakyThrows
    public static <T> HttpResponse<T> call(HttpClient client, HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        return client.send(request, responseBodyHandler);
    }

    @SneakyThrows
    public static <T> HttpResponse<T> call(HttpClient client, HttpRequest request, JavaType javaType) {
        HttpResponse.BodyHandler<T> responseBodyHandler = ResponseUtils.generic(javaType);
        return client.send(request, responseBodyHandler);
    }

    private static <T> T decode(String response, JavaType javaType) {
        return JacksonUtils.readValue(response, javaType);
    }

    /// This action will not be parsed if the status is not 200: to ensure resource safety
    /// Some people forget that HTTP always returns the status and headers first, then the body
    /// So, if you check it not ok first no parse, this can help ensure resource safety
    @SneakyThrows
    public static <T> Optional<T> getBody(HttpResponse<T> response) {
        if (response.statusCode() != 200)
            return Optional.empty();
        return Optional.of(response.body());
    }

    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Auth {
        public static Authenticator basic(String username, String password) {
            return new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password.toCharArray());
                }
            };
        }
    }

    @NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
    public static class Async {
        public static void fireAndForget(HttpClient client, Object data, String url, String token) {
            var request = RequestUtils.post(JacksonUtils.toJson(data), url, token);
            var response = HttpResponse.BodyHandlers.discarding();

            client.sendAsync(request, response);
        }

        public static void fireAndForget(HttpClient client, Object data, String url) {
            var request = RequestUtils.post(JacksonUtils.toJson(data), url);
            var response = HttpResponse.BodyHandlers.discarding();

            client.sendAsync(request, response);
        }

        public static <T> T read(HttpClient client, HttpRequest request, JavaType javaType) {
            HttpResponse.BodyHandler<T> response = ResponseUtils.generic(javaType);
            return client.sendAsync(request, response).join().body();
        }

        public static <T> Optional<T> readAndCheckStatus(HttpClient client, HttpRequest request, JavaType javaType) {
            HttpResponse.BodyHandler<T> response = ResponseUtils.generic(javaType);
            return getBody(client.sendAsync(request, response).join());
        }

        public static <T> HttpResponse<T> call(HttpClient client, HttpRequest request, JavaType javaType) {
            HttpResponse.BodyHandler<T> response = ResponseUtils.generic(javaType);
            return client.sendAsync(request, response).join();
        }
    }
}


