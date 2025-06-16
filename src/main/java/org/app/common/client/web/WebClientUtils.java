package org.app.common.client.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientUtils {
    public static <T> Mono<T> GET(WebClient webClient, String uri, Class<T> responseType) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType);
    }

    public static <T> Mono<T> POST(WebClient webClient, String uri, Object request, Class<T> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType);
    }

    public static <T> Mono<T> PUT(WebClient webClient, String uri, Object request, Class<T> responseType) {
        return webClient.put()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType);
    }

    public static <T> Mono<T> DELETE(WebClient webClient, String uri, Class<T> responseType) {
        return webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType);
    }
}

