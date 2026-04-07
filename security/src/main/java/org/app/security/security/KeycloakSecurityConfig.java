package org.app.security.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.app.security.security.opa.OpaFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    prePostEnabled = true
)
@Order(100) // Lower order value for higher priority
@RequiredArgsConstructor
@Import({OpaFilter.class, UrlRegistry.class, KeycloakJwtAuthenticationConverter.class})
@Profile("keycloak")
// This config will be active when 'keycloak' profile is active
public class KeycloakSecurityConfig {

    @Bean
    public SecurityFilterChain keycloakSecurityFilterChain(
        HttpSecurity http,
        OpaFilter opaFilter,
        UrlRegistry registry,
        KeycloakJwtAuthenticationConverter jwtConvert
    ) throws Exception {
        http
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            .cors().and()
            .csrf().disable()
            .addFilterAfter(opaFilter, BearerTokenAuthenticationFilter.class)
            .authorizeHttpRequests(registry::build)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(j -> j.jwtAuthenticationConverter(jwtConvert)))
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(SecurityBuilder::authEntryPointHandler)
                .accessDeniedHandler(SecurityBuilder::accessDeniedHandler))
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        var roleHierarchy = String.format("%s > %s", UserRole.ROLE_ADMIN, UserRole.ROLE_USER);

        log.info("Role hierarchy configured -- {}", roleHierarchy);

        var hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy(roleHierarchy);
        return hierarchy;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
