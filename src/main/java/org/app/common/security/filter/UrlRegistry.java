package org.app.common.security.filter;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.app.common.security.properties.RoleProperties;
import org.app.common.security.properties.WhiteListProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@EnableConfigurationProperties(value = {RoleProperties.class, WhiteListProperties.class})
@RequiredArgsConstructor
public class UrlRegistry implements Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry> {

    private final RoleProperties roleProperties;

    private final WhiteListProperties whiteListProperties;

    public static final String[] WHITE_LIST = new String[]{
        // Actuator endpoints ADMIN only
        "/actuator/**",
        // Monitoring
        "/refresh", "/prometheus", "/metrics", "/health/**",
        // Auth endpoints
        "/auth/login", "/auth/logout", "/auth/register",
        // Swagger / Docs
        "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/swagger-ui.html"                                                                                   // static resources
    };

    @Override
    public void customize(AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) {
        RequestMatcher[] whiteMatchers = Arrays.stream(combineWhiteList())
            .map(AntPathRequestMatcher::new)
            .toArray(AntPathRequestMatcher[]::new);
        authorize.requestMatchers(whiteMatchers).permitAll();

        roleProperties.getRoles().forEach(role -> {
            String name = role.getName();
            String[] urls = role.getUrls();

            if (name != null && name.startsWith(roleProperties.getPrefix())) {
                authorize.antMatchers(urls).hasRole(name.substring(roleProperties.prefixLength()));
            } else {
                authorize.antMatchers(urls).hasAuthority(name);
            }
        });

        authorize.anyRequest().authenticated();
    }

    private String[] combineWhiteList() {
        return ArrayUtils.addAll(WHITE_LIST, whiteListProperties.getWhiteList().toArray(new String[0]));
    }
}
