package org.app.security.security.opa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opa")
@Getter
@Setter
public class OpaProperties {
    String url = "http://localhost:8181";
    String version = "v1/data";
    String domain = "app";
    String policyPath = "auth/allow";

    public String getUri() {
        return String.join("/", url, version, domain, policyPath);
    }
}
