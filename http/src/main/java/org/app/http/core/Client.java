<<<<<<<< HEAD:http/src/main/java/org/app/http/Client.java
package org.app.http;
========
package org.app.http.core;
>>>>>>>> dev:http/src/main/java/org/app/http/core/Client.java

import com.fasterxml.jackson.databind.JavaType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
<<<<<<<< HEAD:http/src/main/java/org/app/http/Client.java
import org.app.http.interceptor.*;
import org.app.http.request.MarkerRequest;
import org.app.http.response.ResponseUtils;
========
import org.app.http.core.interceptor.*;
import org.app.http.core.request.MarkerRequest;
import org.app.http.core.response.ResponseUtils;
>>>>>>>> dev:http/src/main/java/org/app/http/core/Client.java
import org.app.jackson.JType;
import org.app.jackson.JacksonUtils;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class Client {

    private final HttpClient client;
    private String baseUrl;

    private Map<String, String> headers;
    private List<HttpInterceptor> interceptors;

    public Client(HttpClient client) {
        this.client = client;
    }

    public Client(int timeout) {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
    }

    public Client(String baseUrl, int timeout) {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
        this.baseUrl = baseUrl;
    }

    public Client(String baseUrl, int timeout, Authenticator authenticator) {
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .authenticator(authenticator)
            .build();
        this.baseUrl = baseUrl;
    }

    public Client interceptor(HttpInterceptor... interceptors) {
        this.interceptors = List.of(interceptors);
        return this;
    }

    public Client header(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>(8);
        }
        headers.put(key, value);
        return this;
    }

    public Client enableLogging(LoggingInterceptor loggingInterceptor) {
        interceptors.add(loggingInterceptor);
        return this;
    }

    public Client basicTokenAuth(String token) {
        BasicTokenInterceptor basicTokenInterceptor = new BasicTokenInterceptor(token);
        if (interceptors == null) {
            interceptors = List.of(basicTokenInterceptor);
        } else {
            interceptors.add(basicTokenInterceptor);
        }
        return this;
    }

    public Client authSupplier(Supplier<String> tokenSupplier) {
        AutoRefreshInterceptor tokenAuthInterceptor = new AutoRefreshInterceptor(tokenSupplier);
        if (interceptors == null) {
            interceptors = List.of(tokenAuthInterceptor);
        } else {
            interceptors.add(tokenAuthInterceptor);
        }
        return this;
    }

    @SneakyThrows
    public <T> HttpResponse<T> post(Object body, String path, JavaType javaType) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(javaType)
                .url(baseUrl + path)
                .body(JacksonUtils.toJson(body))
                .method("POST")
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> get(String path) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(JType.VOID)
                .url(baseUrl + path)
                .method("GET")
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> put(Object body, String path, JavaType javaType) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(javaType)
                .url(baseUrl + path)
                .body(JacksonUtils.toJson(body))
                .method("PUT")
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> delete(String path) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(JType.VOID)
                .url(baseUrl + path)
                .method("DELETE")
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> getAsync(String path) {
        return responseOf(
            MarkerRequest.builder()
                .async(true)
                .javaType(JType.VOID)
                .url(baseUrl + path)
                .method("GET")
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> postAsync(Object body, String path, JavaType javaType) {
        return responseOf(
            MarkerRequest.builder()
                .async(true)
                .javaType(javaType)
                .url(baseUrl + path)
                .body(JacksonUtils.toJson(body))
                .method("POST")
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> putAsync(Object body, String path, JavaType javaType) {
        return responseOf(
            MarkerRequest.builder()
                .async(true)
                .javaType(javaType)
                .url(baseUrl + path)
                .body(JacksonUtils.toJson(body))
                .method("PUT")
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> deleteAsync(String path) {
        return responseOf(
            MarkerRequest.builder()
                .async(true)
                .javaType(JType.VOID)
                .url(baseUrl + path)
                .method("DELETE")
                .headers(headers)
                .build()
        );
    }

    @SneakyThrows
    private <T> HttpResponse<T> responseOf(MarkerRequest wrapper) {
        if (interceptors != null && !interceptors.isEmpty()) {
            return new ChainLink(interceptors.iterator(), client).proceed(wrapper);
        }

        if (wrapper.isAsync()) {
            return ClientUtils.Async.call(
                client,
                wrapper.getRequestBuilder().build(),
                wrapper.getJavaType()
            );
        }

        return client.send(wrapper.getRequestBuilder().build(), ResponseUtils.generic(wrapper.getJavaType()));
    }
}
