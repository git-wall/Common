package org.app.security.security;

import lombok.RequiredArgsConstructor;
import org.app.security.security.properties.CorsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
public class CorsConfig {

    /**
     * Use this if you want to allow origins, stop to access API
     * Example for use this
     * <pre>{@code
     *   app.cors.allowed.originPatterns=https://example.com,https://another-domain.com
     * }</pre>
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.applyPermitDefaultValues();

        // allow domain you want
        // When both are set, allowedOriginPatterns takes precedence than AllowedOrigins
        configuration.setAllowedOriginPatterns(corsProperties.getOriginPatterns());
//        configuration.setAllowedOrigins();

        // allow cookie/session
        configuration.setAllowCredentials(true);
        // allow method
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        // allow header
        configuration.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
