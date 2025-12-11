package org.app.common.opa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opa")
@Getter
@Setter
public class OpaProperties {
    /**
     * Base URL for OPA API, use %s as placeholder for policy path.
     * Example: http://localhost:8181/v1/data/%s
     */
    private String url = "http://localhost:8181/v1/data/%s";

    /**
     * Default policy path to check.
     * Example: auth/allow
     */
    private String policyPath = "auth/allow";

    public String getUri() {
        return String.format(url, policyPath);
    }
}
