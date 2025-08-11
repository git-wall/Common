package org.app.common.security.filter;

import lombok.RequiredArgsConstructor;
import org.app.common.security.properties.RoleProperties;
import org.app.common.security.properties.WhiteListProperties;
import org.app.common.support.ArrayProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(value = {RoleProperties.class, WhiteListProperties.class})
@RequiredArgsConstructor
public class UrlRegistry implements Customizer<ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry> {

    private final RoleProperties roleProperties;

    private final WhiteListProperties whiteListProperties;

    public static final String[] WHITE_LIST = new String[]{
            "/actuator/**", "/refresh", "/prometheus", "/metrics", "/health/**",                             // monitor
            "/auth/login", "/auth/logout", "/auth/register",                                                 // customer
            "/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**", "/swagger-ui.html", // swagger
    };

    @Override
    public void customize(ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry authorize) {
        String[] whiteList = combineWhiteList();
        authorize.antMatchers(whiteList).permitAll();
        roleProperties.getRoles().forEach(role -> authorize.antMatchers(role.getUrls()).hasAuthority(role.getName()));
        authorize.anyRequest().authenticated();
    }

    private String[] combineWhiteList() {
        String[] whiteList2 = whiteListProperties.getWhiteList().split("");
        return ArrayProvider.combine(WHITE_LIST, whiteList2);
    }
}
