package org.app.common.opa;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

@Configuration
@EnableWebSecurity
public class OpaSecurityConfig extends WebSecurityConfigurerAdapter {
    
    private final OpaClient opaClient;
    
    public OpaSecurityConfig(OpaClient opaClient) {
        this.opaClient = opaClient;
    }
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .anyRequest().authenticated()
            .and()
            .addFilterAfter(opaAuthorizationFilter(), FilterSecurityInterceptor.class)
            .csrf().disable();
    }
    
    @Bean
    public OpaAuthorizationFilter opaAuthorizationFilter() {
        return new OpaAuthorizationFilter(opaClient);
    }
}