package org.app.common.client.web;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

@Configuration
public class WebClientConfig {

    @Value("${client.timeout.second}")
    private Integer clientTimeout;

    @Bean
    @ConditionalOnProperty(name = "client.timeout.second")
    public HttpClient httpClient(@Qualifier("logicThreadPool") ExecutorService executorService) {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(clientTimeout))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .executor(executorService)
                .build();
    }
}
