package org.app.common.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.role")
public class RoleProperties {
    /**
     * Optional prefix used to detect or strip role names (e.g. "ROLE_").
     * Default is "ROLE_". You can override it per service.
     */
    private String prefix = "ROLE_";

    private List<Role> roles;

    public int prefixLength() {
        return prefix.length();
    }

    @Getter
    @Setter
    public static class Role {
        private String name;
        private String[] urls;
    }
}
