package org.app.common.client.web;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@AllArgsConstructor
public class WebClientTemplate {
    private String baseUrl;

    private String authUrl;

    private String clientId;

    private String clientSecret;

    private int connectTimeout;

    private long readTimeout;

    // Token store
    private final AtomicReference<TokenInfo> tokenInfo = new AtomicReference<>(new TokenInfo());

    // Semaphore to prevent multiple concurrent token refreshes
    private final Object refreshLock = new Object();

    private WebClient authWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(authUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .filter(logRequest())
                .build();
    }

    public WebClient build() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS)));

        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(refreshTokenFilter(authWebClient()))
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction refreshTokenFilter(WebClient authWebClient) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
                        Mono.just(ClientRequest.from(clientRequest)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + getOrFetchAccessToken(authWebClient).accessToken)
                                .build())
                )
                .andThen((request, next) ->
                        next.exchange(request).flatMap(clientResponse -> {
                            if (clientResponse.statusCode() == HttpStatus.UNAUTHORIZED) {
                                return clientResponse.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            // Force refresh token
                                            TokenInfo newToken = refreshToken(authWebClient);

                                            if (newToken.accessToken != null) {
                                                // Retry the request with the new token
                                                ClientRequest newRequest = ClientRequest.from(request)
                                                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken.accessToken)
                                                        .build();
                                                return next.exchange(newRequest);
                                            }

                                            // If token refresh failed, return original response
                                            return Mono.just(clientResponse);
                                        });
                            }
                            return Mono.just(clientResponse);
                        })
                );
    }

    // Helper method to get current token or fetch a new one if not present
    private TokenInfo getOrFetchAccessToken(WebClient authWebClient) {
        TokenInfo current = tokenInfo.get();

        // Check if access token exists and is not expired
        if (current.accessToken != null && current.expiresAt > System.currentTimeMillis()) {
            return current;
        }

        // We need a new token
        synchronized (refreshLock) {
            // Double-check in case another thread refreshed while we were waiting
            current = tokenInfo.get();
            if (current.accessToken != null && current.expiresAt > System.currentTimeMillis()) {
                return current;
            }

            // Actually do the refresh
            return refreshToken(authWebClient);
        }
    }

    // Refresh token and return new token info
    private TokenInfo refreshToken(WebClient authWebClient) {
        TokenInfo current = tokenInfo.get();
        TokenInfo newTokenInfo = new TokenInfo();

        try {
            if (current.refreshToken != null) {
                // Try to use refresh token
                newTokenInfo = authWebClient.post()
                        .uri("/oauth/token")
                        .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                                .with("refresh_token", current.refreshToken)
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(this::convertToTokenInfo)
                        .blockOptional()
                        .orElse(new TokenInfo());
            }

            // If refresh token approach failed, try client credentials
            if (newTokenInfo.accessToken == null) {
                newTokenInfo = authWebClient.post()
                        .uri("/oauth/token")
                        .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                                .with("client_id", clientId)
                                .with("client_secret", clientSecret))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(this::convertToTokenInfo)
                        .blockOptional()
                        .orElse(new TokenInfo());
            }

            // Store the new token
            if (newTokenInfo.accessToken != null) {
                tokenInfo.set(newTokenInfo);
            }

            return newTokenInfo;
        } catch (Exception e) {
            log.error("Error refreshing token", e);
            return new TokenInfo();
        }
    }

    // Convert API response to TokenInfo
    private TokenInfo convertToTokenInfo(Map<String, Object> response) {
        TokenInfo info = new TokenInfo();
        info.accessToken = (String) response.get("access_token");
        info.refreshToken = (String) response.get("refresh_token");

        Integer expiresIn = (Integer) response.get("expires_in");
        if (expiresIn != null) {
            // Calculate expiration time with a small buffer (5 seconds)
            info.expiresAt = System.currentTimeMillis() + (long) (expiresIn * 1000) - 5000L;
        }

        return info;
    }

    // Logging filter
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> {
                if (!name.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
                    values.forEach(value -> log.debug("{}={}", name, value));
                } else {
                    log.debug("{}=****", name);
                }
            });
            return Mono.just(clientRequest);
        });
    }

    // Class to store token information
    private static class TokenInfo {
        String accessToken;
        String refreshToken;
        long expiresAt;
    }
}
