package org.app.common.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.role")
public class RoleProperties {
    private List<Role> roles;

    @Getter
    @Setter
    public static class Role {
        private String name;
        private String[] urls;
    }
}
