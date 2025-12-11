package org.app.common.client.http;

import com.fasterxml.jackson.databind.JavaType;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.app.common.client.http.interceptor.AutoRefreshInterceptor;
import org.app.common.client.http.interceptor.BasicTokenInterceptor;
import org.app.common.client.http.interceptor.LoggingInterceptor;
import org.app.common.client.http.request.MarkerRequest;
import org.app.common.client.http.response.HttpResponseUtils;
import org.app.common.constant.JType;
import org.app.common.context.SpringContext;
import org.app.common.utils.JacksonUtils;
import org.springframework.http.HttpMethod;

import java.net.Authenticator;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
public class WebClient {

    private final HttpClient client;
    private String baseUrl;

    private Map<String, String> headers;
    private List<HttpInterceptor> interceptors;

    public WebClient(HttpClient client) {
        this.client = client;
    }

    public WebClient(int timeout) {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
    }

    public WebClient(String baseUrl, int timeout) {
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(timeout)).build();
        this.baseUrl = baseUrl;
    }

    public WebClient(String baseUrl, int timeout, Authenticator authenticator) {
        this.client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(timeout))
            .authenticator(authenticator)
            .build();
        this.baseUrl = baseUrl;
    }

    public WebClient interceptor(HttpInterceptor... interceptors) {
        this.interceptors = List.of(interceptors);
        return this;
    }

    public WebClient header(String key, String value) {
        if (headers == null) {
            headers = new HashMap<>(8);
        }
        headers.put(key, value);
        return this;
    }

    public WebClient enableLogging() {
        interceptors.add(SpringContext.getBean(LoggingInterceptor.class));
        return this;
    }

    public WebClient basicTokenAuth(String token) {
        BasicTokenInterceptor basicTokenInterceptor = new BasicTokenInterceptor(token);
        if (interceptors == null) {
            interceptors = List.of(basicTokenInterceptor);
        } else {
            interceptors.add(basicTokenInterceptor);
        }
        return this;
    }

    public WebClient authSupplier(Supplier<String> tokenSupplier) {
        AutoRefreshInterceptor tokenAuthInterceptor = new AutoRefreshInterceptor(tokenSupplier);
        if (interceptors == null) {
            interceptors = List.of(tokenAuthInterceptor);
        } else {
            interceptors.add(tokenAuthInterceptor);
        }
        return this;
    }

    public void sendAsync(HttpMethod method, Object body, String path) {
        switch (method) {
            case POST:
                postAsync(body, path, null);
                break;
            case GET:
                getAsync(path);
                break;
            case PUT:
                putAsync(body, path, null);
                break;
            case DELETE:
                deleteAsync(path);
                break;
            default:
                throw new UnsupportedOperationException("Method not supported: " + method);
        }
    }

    public <T> T read(HttpMethod method, Object body, String path, JavaType javaType) {
        return this.<T>call(method, body, path, javaType).body();
    }

    public <T> T readAsync(HttpMethod method, Object body, String path, JavaType javaType) {
        return this.<T>callAsync(method, body, path, javaType).body();
    }

    public <T> HttpResponse<T> call(HttpMethod method, Object body, String path, JavaType javaType) {
        switch (method) {
            case POST:
                return post(body, path, javaType);
            case GET:
                return get(path);
            case PUT:
                return put(body, path, javaType);
            case DELETE:
                return delete(path);
            default:
                throw new UnsupportedOperationException("Method not supported: " + method);
        }
    }

    public <T> HttpResponse<T> callAsync(HttpMethod method, Object body, String path, JavaType javaType) {
        switch (method) {
            case POST:
                return postAsync(body, path, javaType);
            case GET:
                return getAsync(path);
            case PUT:
                return putAsync(body, path, javaType);
            case DELETE:
                return deleteAsync(path);
            default:
                throw new UnsupportedOperationException("Method not supported: " + method);
        }
    }

    @SneakyThrows
    public <T> HttpResponse<T> post(Object body, String path, JavaType javaType) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(javaType)
                .url(baseUrl + path)
                .body(JacksonUtils.toJson(body))
                .method(HttpMethod.POST.name())
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
                .method(HttpMethod.GET.name())
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
                .method(HttpMethod.PUT.name())
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
                .method(HttpMethod.DELETE.name())
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> getAsync(String path) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(JType.VOID)
                .url(baseUrl + path)
                .method(HttpMethod.GET.name())
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> postAsync(Object body, String path, JavaType javaType) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(javaType)
                .url(baseUrl + path)
                .body(JacksonUtils.toJson(body))
                .method(HttpMethod.POST.name())
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> putAsync(Object body, String path, JavaType javaType) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(javaType)
                .url(baseUrl + path)
                .body(JacksonUtils.toJson(body))
                .method(HttpMethod.PUT.name())
                .headers(headers)
                .build()
        );
    }

    public <T> HttpResponse<T> deleteAsync(String path) {
        return responseOf(
            MarkerRequest.builder()
                .async(false)
                .javaType(JType.VOID)
                .url(baseUrl + path)
                .method(HttpMethod.DELETE.name())
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
            return client
                .<T>sendAsync(wrapper.getRequestBuilder().build(), HttpResponseUtils.generic(wrapper.getJavaType()))
                .join();
        }

        return client.send(wrapper.getRequestBuilder().build(), HttpResponseUtils.generic(wrapper.getJavaType()));
    }
}

@AllArgsConstructor
class ChainLink implements HttpInterceptor.Chain {
    private final Iterator<HttpInterceptor> iterators;
    private final HttpClient client;

    @Override
    @SneakyThrows
    public <T> HttpResponse<T> proceed(MarkerRequest wrapper) {
        if (iterators.hasNext()) {
            HttpInterceptor interceptor = iterators.next();
            return interceptor.intercept(wrapper, this, client);
        }

        if (wrapper.isAsync()) {
            return client
                .<T>sendAsync(wrapper.getRequestBuilder().build(), HttpResponseUtils.generic(wrapper.getJavaType()))
                .join();
        }

        return client.send(wrapper.getRequestBuilder().build(), HttpResponseUtils.generic(wrapper.getJavaType()));
    }
}

