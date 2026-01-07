package org.app.common.security.chain;

import lombok.RequiredArgsConstructor;
import org.app.common.security.filter.AuthBeforeFilter;
import org.app.common.security.filter.ServiceValidationFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableConfigurationProperties(SecurityConfigProperties.class)
@Import({AuthBeforeFilter.class, ServiceValidationFilter.class})
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.security.internal-api", name = "enabled", havingValue = "true", matchIfMissing = true)
public class InternalSecurityConfig extends BaseSecurityFilterChainBuilder {

    private final SecurityConfigProperties properties;
    private final AuthBeforeFilter authBeforeFilter;
    private final ServiceValidationFilter serviceValidationFilter;

    @Bean
    @Order(2)
    @ConditionalOnBean(HttpSecurity.class)
    public SecurityFilterChain internalApiSecurityFilterChain(HttpSecurity http) throws Exception {
        chainLink(http);
        return build(http);
    }

    private void chainLink(HttpSecurity http) throws Exception {
        http.antMatcher(properties.getInternalApi().getPathPattern())
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .addFilterBefore(authBeforeFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(serviceValidationFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
