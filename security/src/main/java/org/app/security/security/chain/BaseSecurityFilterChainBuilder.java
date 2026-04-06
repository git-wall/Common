package org.app.security.security.chain;

import org.app.security.security.SecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

public abstract class BaseSecurityFilterChainBuilder {
    public SecurityFilterChain build(HttpSecurity http) throws Exception {
        http.cors().and()
            .csrf().disable()
            .httpBasic().disable()
            .formLogin().disable()
            .oauth2Login().disable()
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(SecurityBuilder::authEntryPointHandler)
                .accessDeniedHandler(SecurityBuilder::accessDeniedHandler)
            )
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}
