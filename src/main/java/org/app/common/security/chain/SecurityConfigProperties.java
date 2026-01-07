package org.app.common.security.chain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@ConfigurationProperties(prefix = "app.security")
@Data
public class SecurityConfigProperties {
    private AdminConfig admin = new AdminConfig();
    private InternalApiConfig internalApi = new InternalApiConfig();
    private List<String> publicApi = new ArrayList<>();

    public AntPathRequestMatcher[] getPublicApiPathMatchers() {
        List<String> defaultWhitelist = List.of("/auth/login", "/auth/register", "/auth/forgot-password");
        publicApi.addAll(defaultWhitelist);
        return publicApi
            .stream()
            .map(AntPathRequestMatcher::new)
            .toArray(AntPathRequestMatcher[]::new);
    }

    @Data
    public static class AdminConfig {
        private boolean enabled = true;
        private List<String> pathPattern = new ArrayList<>();
        private List<RoleMapping> roles = new ArrayList<>();

        public List<RequestMatcher> getPathMatchers() {
            return pathPattern
                .stream()
                .map(AntPathRequestMatcher::new)
                .collect(Collectors.toList());
        }
    }

    @Data
    public static class InternalApiConfig {
        private boolean enabled = true;
        private String pathPattern = "/internal/**";
        private List<String> allowedServices = new ArrayList<>();
    }

    @Data
    public static class RoleMapping {
        private String name;
        private List<String> urls;
        private String type = "ROLE"; // ROLE or AUTHORITY
    }
}
