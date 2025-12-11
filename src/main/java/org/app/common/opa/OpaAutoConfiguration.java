package org.app.common.opa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.app.common.client.http.WebClient;

@Configuration
@EnableConfigurationProperties(OpaProperties.class)
public class OpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpaInputBuilder opaInputBuilder() {
        return new DefaultOpaInputBuilder();
    }

    @Bean
    @ConditionalOnBean(WebClient.class)
    public OpaClient opaClient(WebClient webClient, OpaProperties opaProperties) {
        return new OpaClient(webClient, opaProperties);
    }

    @Bean
    public OpaAuthorizationFilter opaAuthorizationFilter(OpaClient opaClient, OpaInputBuilder inputBuilder) {
        return new OpaAuthorizationFilter(opaClient, inputBuilder);
    }
}
