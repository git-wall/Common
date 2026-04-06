package org.app.security.security.chain;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import javax.annotation.security.RolesAllowed;

@RequiredArgsConstructor
@Configuration
@EnableConfigurationProperties(SecurityConfigProperties.class)
@ConditionalOnProperty(prefix = "app.security.admin", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AdminSecurityConfig extends BaseSecurityFilterChainBuilder{

    protected final SecurityConfigProperties properties;

    @Bean
    @Order(1)
    @ConditionalOnBean(HttpSecurity.class)
    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http) throws Exception {
        chainLink(http);
        return build(http);
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
    private void chainLink(HttpSecurity http) throws Exception {
        var adminConfig = properties.getAdmin();
        // role based authorization
        http.authorizeHttpRequests(auth -> {
            // Role-based authorization
            adminConfig.getRoles().forEach(role -> {
                String name = role.getName();
                String[] urls = role.getUrls().toArray(new String[0]);

                auth.antMatchers(urls).hasAuthority(name);
            });

            auth.anyRequest().authenticated();
        });
    }
}
