package org.app.common.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;

@EnableWebMvc
@Configuration
@RefreshScope
public class WebConfig implements WebMvcConfigurer {

    @Value("${rest.timeout.second}")
    private long restTimeout;

    @Value("${client.timeout.second}")
    private long clientTimeout;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    @Bean
    @ConditionalOnProperty(name = "rest.timeout.second")
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(restTimeout))
                .setReadTimeout(Duration.ofSeconds(restTimeout))
                .build();
    }

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