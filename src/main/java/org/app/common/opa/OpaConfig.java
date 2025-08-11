package org.app.common.opa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpaConfig {
    
    @Value("${opa.url:http://localhost:8181/v1/data/%s}")
    private String opaUrl;
    
    @Bean
    @ConditionalOnBean(RestTemplate.class)
    public OpaClient opaClient(RestTemplate restTemplate) {
        return new OpaClient(restTemplate, opaUrl);
    }
}