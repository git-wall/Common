package org.app.common.module.call_center.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for the Call Center module
 */
@Configuration
@EnableRetry
public class CallCenterConfig {

    /**
     * Create a RestTemplate bean for API calls
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
