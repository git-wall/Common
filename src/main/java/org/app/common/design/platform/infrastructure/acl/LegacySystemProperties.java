package org.app.common.design.platform.infrastructure.acl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "legacy.system")
@Data
public class LegacySystemProperties {
    private String baseUrl;      // http://legacy-system.company.com
    private String apiKey;
    private int timeout;
    private int retryAttempts;
}
