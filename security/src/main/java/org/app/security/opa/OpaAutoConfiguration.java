<<<<<<<< HEAD:security/src/main/java/org/app/security/security/opa/OpaAutoConfiguration.java
package org.app.security.security.opa;
========
package org.app.security.opa;
>>>>>>>> dev:security/src/main/java/org/app/security/opa/OpaAutoConfiguration.java

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OpaProperties.class)
public class OpaAutoConfiguration {

    @Bean
    @ConditionalOnBean(WebClient.class)
    public OpaClient opaClient(WebClient webClient, OpaProperties opaProperties) {
        return new OpaClient(webClient, opaProperties);
    }

    @Bean
    public OpaFilter opaAuthorizationFilter(OpaClient opaClient) {
        return new OpaFilter(opaClient);
    }
}
