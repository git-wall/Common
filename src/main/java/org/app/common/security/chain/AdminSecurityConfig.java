package org.app.common.security.chain;

import lombok.RequiredArgsConstructor;
import org.app.common.security.filter.AuthBeforeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import javax.annotation.security.RolesAllowed;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(SecurityConfigProperties.class)
@Import({AuthBeforeFilter.class})
@ConditionalOnProperty(prefix = "app.security.admin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AdminSecurityConfig extends BaseSecurityFilterChainBuilder{

    protected final SecurityConfigProperties properties;
    protected final AuthBeforeFilter authBeforeFilter;

    @Bean
    @Order(1)
    @ConditionalOnBean(HttpSecurity.class)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        chainLink(http);
        RBAC(http);
        return build(http);
    }

    private void chainLink(HttpSecurity http) throws Exception {
        http.requestMatcher(new OrRequestMatcher(properties.getAdmin().getPathMatchers()))
            .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
            .addFilterBefore(authBeforeFilter, UsernamePasswordAuthenticationFilter.class);
    }

    // Role-Based Access Control
    /**
     * <pre>{@code
     *     // @EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
     *     // Role-based authorization example
     *     @Secured("ROLE_ADMIN")
     *     @RolesAllowed("ADMIN")
     *     @PreAuthorize("hasRole('ADMIN')")
     *     @PreAuthorize("hasAuthority('ORDER_WRITE')")
     * }</pre>
     * Use -> {@link Secured}, {@link RolesAllowed}, {@link PreAuthorize}, {@link PreAuthorize}
     * */
    private void RBAC(HttpSecurity http) throws Exception {
        var adminConfig = properties.getAdmin();
        // role based authorization
        http.authorizeHttpRequests(auth -> {
            // Role-based authorization
            adminConfig.getRoles().forEach(role -> {
                String name = role.getName();
                String[] urls = role.getUrls().toArray(new String[0]);

                if ("ROLE".equals(role.getType())) {
                    auth.antMatchers(urls).hasRole(name);
                } else {
                    auth.antMatchers(urls).hasAuthority(name);
                }
            });

            // Các request còn lại cần authentication
            auth.anyRequest().authenticated();
        });
    }
}
