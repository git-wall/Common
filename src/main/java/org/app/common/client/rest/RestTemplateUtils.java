package org.app.common.client.rest;

import org.app.common.context.SpringContext;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.function.Function;

public class RestTemplateUtils {

    public static RestTemplate build(int timeout, ClientHttpRequestInterceptor... interceptors) {
        RestTemplateBuilder builder = new RestTemplateBuilder();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);

        BufferingClientHttpRequestFactory bufferingRequestFactory = new BufferingClientHttpRequestFactory(requestFactory);

        return builder
                .requestFactory(() -> bufferingRequestFactory)
                .interceptors(interceptors)
                .build();
    }

    public static <T> T fetch(String url, Object request, Class<T> clazz) {
        RestTemplate client = SpringContext.getContext().getBean(RestTemplate.class);
        return RestfulApi.<T>of(client)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .get();
    }

    public static <T, R> R fetchAndConvert(String url, Object request, Function<T, R> function) {
        RestTemplate client = SpringContext.getContext().getBean(RestTemplate.class);
        T res = RestfulApi.<T>of(client)
                .method(HttpMethod.POST)
                .request(request)
                .url(url)
                .exchange()
                .get();
        return function.apply(res);
    }
}
