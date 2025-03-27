package org.app.common.client.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientUtils {
    public static <T> Mono<T> getResource(WebClient webClient, String path, Class<T> responseType) {
        return webClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(responseType);
    }

    public static <T> Mono<T> postResource(WebClient webClient, String path, Object request, Class<T> responseType) {
        return webClient.post()
                .uri(path)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType);
    }
}

