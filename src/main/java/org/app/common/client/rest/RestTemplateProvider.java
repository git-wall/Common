package org.app.common.client.rest;

import org.app.common.context.SpringContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

public class RestTemplateProvider {
    public static <T> T POST(RestTemplate client, String url, Object request) {
        return RestfulApi.<T>of(client)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .get();
    }

    public static <T> T POST(RestTemplate client, String url, Object request, String token) {
        return RestfulApi.<T>ofAuth(client, token)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .get();
    }

    public static <T> T POSTCovert(RestTemplate client, String url, Object request, Class<T> clazz) {
        return RestfulApi.<T>of(client)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .sink(clazz);
    }

    public static <T> T POST(String url, Object request) {
        RestTemplate client = SpringContext.getBean(RestTemplate.class);
        return RestfulApi.<T>of(client)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .get();
    }

    public static <T, R> R POSTCovert(String url, Object request, Function<T, R> function) {
        RestTemplate client = SpringContext.getBean(RestTemplate.class);
        T res = RestfulApi.<T>of(client)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .get();
        return function.apply(res);
    }

    public static <T> T POSTCovert(String url, Object request, Class<T> clazz) {
        RestTemplate client = SpringContext.getBean(RestTemplate.class);
        return RestfulApi.<T>of(client)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .sink(clazz);
    }
}
