package org.app.common.client.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
public class WebClientUtils {
    public static <T> T GET(WebClient webClient, String uri, Class<T> responseType) {
        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public static <T> T POST(WebClient webClient, String uri, Object request, Class<T> responseType) {
        return webClient.post()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public static <T> T PUT(WebClient webClient, String uri, Object request, Class<T> responseType) {
        return webClient.put()
                .uri(uri)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    public static <T> T DELETE(WebClient webClient, String uri, Class<T> responseType) {
        return webClient.delete()
                .uri(uri)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }
}

