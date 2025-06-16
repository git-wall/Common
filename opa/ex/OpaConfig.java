package org.app.common.opa;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class OpaConfig {
    
    @Value("${opa.url:http://localhost:8181}")
    private String opaUrl;
    
    @Bean
    public RestTemplate opaRestTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public OpaClient opaClient(RestTemplate opaRestTemplate) {
        return new OpaClient(opaRestTemplate, opaUrl);
    }
}