package org.app.common.client.web;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.app.common.context.TracingContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WebClientFactory {

    public WebClient buildRefreshTokenClient(String baseUrl,
                                             String authUrl,
                                             String clientId,
                                             String clientSecret,
                                             int connectTimeout,
                                             long readTimeout) {
        return new WebClientTemplate(baseUrl, authUrl, clientId, clientSecret, connectTimeout, readTimeout)
                .build();
    }

    public WebClient buildAuthClient(String authUrl, long timeout) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(authUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .filter(logRequest())
                .build();
    }

    public static WebClient buildClient(String baseUrl, long timeout, String authToken) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) timeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(authenticationFilter(authToken))
                .filter(logRequest())
                .filter(logResponse())
                .build();
    }

    public static ExchangeFilterFunction authenticationFilter(String authToken) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (authToken != null && !authToken.isEmpty()) {
                HttpHeaders headers = clientRequest.headers();
                headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authToken);
                return Mono.just(clientRequest);
            }
            return Mono.just(clientRequest);
        });
    }

    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction
                .ofRequestProcessor(clientRequest -> {
                    log.info(
                            "{} - URL: {}, Method: {}, Header: {} , Body: {}",
                            TracingContext.getRequestId(),
                            clientRequest.url(),
                            clientRequest.method(),
                            clientRequest.headers(),
                            clientRequest.body()
                    );
                    return Mono.just(clientRequest);
                });
    }

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse ->
                clientResponse.bodyToMono(Object.class)
                        .doOnNext(body -> log.info(
                                "{} - Status: {}, Response Body: {}",
                                TracingContext.getRequestId(),
                                clientResponse.statusCode(),
                                body
                        ))
                        .then(Mono.just(clientResponse)));
    }
}
