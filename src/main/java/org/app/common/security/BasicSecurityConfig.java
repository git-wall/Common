package org.app.common.security;

import lombok.RequiredArgsConstructor;
import org.app.common.security.filter.AuthBeforeFilter;
import org.app.common.security.filter.UrlRegistry;
import org.app.common.utils.RequestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
@Order(101)
@RequiredArgsConstructor
@Import({AuthBeforeFilter.class, UrlRegistry.class})
@Profile("!keycloak")
public class BasicSecurityConfig {

    private final AuthBeforeFilter authBeforeFilter;

    private final UrlRegistry urlRegistry;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf().disable()
                .formLogin(Customizer.withDefaults())
                .oauth2Login(Customizer.withDefaults())
                .addFilterBefore(authBeforeFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeRequests(urlRegistry)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(RequestUtils::authEntryPointHandler)
                        .accessDeniedHandler(RequestUtils::accessDeniedHandler)
                )
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}
