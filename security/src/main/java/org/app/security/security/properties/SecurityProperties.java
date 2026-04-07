package org.app.security.security.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>{@code
 * security:
 *   groups:
 *     public:
 *       access: PERMIT_ALL
 *       urls: [/health/**]
 *
 *     admin:
 *       access: HAS_ROLE
 *       roles: [ROLE_ADMIN]
 *       urls: [/admin/**]
 * }</pre>
 * */
@Data
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private Map<String, SecurityGroup> groups = new LinkedHashMap<>();

    @Data
    public static class SecurityGroup {
        private AccessType access;
        private List<String> urls = new ArrayList<>();
        private List<String> roles = new ArrayList<>();

        public RequestMatcher[] paths() {
            return urls.stream()
                .map(AntPathRequestMatcher::new)
                .toArray(RequestMatcher[]::new);
        }
    }

    public enum AccessType {
        PERMIT_ALL,
        AUTHENTICATED,
        HAS_AUTH
    }
}
