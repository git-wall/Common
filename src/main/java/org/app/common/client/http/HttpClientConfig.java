package org.app.common.client.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Value("${http.client.connectTimeout:5000}")
    private int connectTimeout;

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2) // HTTP/2 if supported Auto downgrade to HTTP/1.1 if not
            .connectTimeout(Duration.ofSeconds(connectTimeout))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    }

    @Bean
    public WebClient webClient(HttpClient client) {
        return new WebClient(client);
    }
}
