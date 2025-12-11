package org.app.common.security;

import lombok.RequiredArgsConstructor;
import org.app.common.security.properties.CorsProperties;
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
     *   app.cors.allowed.origins=https://example.com,https://another-domain.com
     * }</pre>
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.applyPermitDefaultValues();

        // allow domain you want
        configuration.setAllowedOrigins(corsProperties.getOrigins());
        // allow cookie/session
        configuration.setAllowCredentials(true);
        // allow method
        configuration.addAllowedMethod(List.of("GET", "POST", "PUT", "DELETE").toString());
        // allow header
        configuration.addAllowedHeader("*");

        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://*._.com",
            "https://_.com"
        ));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
