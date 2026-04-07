package org.app.security.security;

import lombok.RequiredArgsConstructor;
import org.app.security.security.properties.SecurityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.util.Assert;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class UrlRegistry {

    private final SecurityProperties props;

    public void build(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth) {
        props.getGroups()
            .forEach((name, group) -> {
                Assert.isTrue(group.getUrls().isEmpty(), String.format("Security group [%s] has no urls", name));

                if (SecurityProperties.AccessType.PERMIT_ALL.equals(group.getAccess()))
                    auth.requestMatchers(group.paths()).permitAll();

                if (SecurityProperties.AccessType.AUTHENTICATED.equals(group.getAccess()))
                    auth.requestMatchers(group.paths()).authenticated();

                if (SecurityProperties.AccessType.HAS_AUTH.equals(group.getAccess())) {
                    Assert.isTrue(group.getRoles().isEmpty(), String.format("HAS_ROLE group [%s] must define roles", name));
                    auth.requestMatchers(group.paths()).hasAnyAuthority(group.getRoles().toArray(String[]::new));
                }
            });

        // DEFAULT DENY
        auth.anyRequest().denyAll();
    }

//    public static final String[] WHAT_URL = new String[]{
//        // Actuator endpoints ADMIN only
//        "/actuator/**",
//        // Monitoring
//        "/refresh", "/prometheus", "/metrics", "/health/**",
//        // Auth endpoints
//        "/auth/login", "/auth/logout", "/auth/register",
//        // Swagger / Docs
//        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/swagger-ui.html"                                                                                   // static resources
//    };
}
