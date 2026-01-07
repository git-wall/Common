package org.app.common.security.chain;

import lombok.RequiredArgsConstructor;
import org.app.common.security.filter.AuthBeforeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableConfigurationProperties(SecurityConfigProperties.class)
@Configuration
@Import({AuthBeforeFilter.class})
@RequiredArgsConstructor
public class PublicSecurityConfig extends BaseSecurityFilterChainBuilder {

    protected final SecurityConfigProperties properties;
    protected final AuthBeforeFilter authBeforeFilter;

    @Bean
    @Order(3)
    @ConditionalOnBean(HttpSecurity.class)
    public SecurityFilterChain publicApiSecurityFilterChain(HttpSecurity http) throws Exception {
        chainLink(http);
        return build(http);
    }

    private void chainLink(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(properties.getPublicApiPathMatchers()).permitAll();
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(authBeforeFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
