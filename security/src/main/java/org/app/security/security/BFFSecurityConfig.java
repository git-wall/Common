package org.app.security.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    prePostEnabled = true
)
@Order(103)
public class BFFSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // when use filter and set data to session, need to disable csrf, formLogin, httpBasic, oauth2Login
    // if you need session, set sessionCreationPolicy to IF_REQUIRED or ALWAYS
    // REST API usually set to STATELESS
    // FORM LOGIN usually set to IF_REQUIRED or ALWAYS
    @Bean
    @ConditionalOnMissingBean({UrlRegistry.class})
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.headers(headers -> headers
                // Reflected XSS (script in URL / query param)
                // nowadays browser stop them outside we don't need setting this
                // .xssProtection(x -> x.block(true))
                // MIME sniffing attack [Content-Type: text/plain -> <script>alert(1)</script>]
                .contentTypeOptions().and()
                // Clickjacking attack
                .frameOptions().deny()
                // Leak important info from HTTP Referer
                .addHeaderWriter(
                    new ReferrerPolicyHeaderWriter(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                    )
                )
                // SSL Stripping / Man-in-the-Middle
                // [User -> http://example.com -> Hacker downgrade HTTPS → HTTP (Token is sniff) ]
                .httpStrictTransportSecurity()
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000)
            )
            .cors().and()
            .csrf().disable()
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(SecurityBuilder::authEntryPointHandler)
                .accessDeniedHandler(SecurityBuilder::accessDeniedHandler)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(j -> j.jwtAuthenticationConverter(jwtAuthenticationConverter())))
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter converter =
            new JwtGrantedAuthoritiesConverter();

        converter.setAuthorityPrefix("");
        converter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter jwtConverter =
            new JwtAuthenticationConverter();

        jwtConverter.setJwtGrantedAuthoritiesConverter(converter);
        return jwtConverter;
    }
}
