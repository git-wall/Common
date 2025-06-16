package org.app.eav.jpa;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jpa.scan")
@Getter
@Setter
public class JpaBaseProperties {
    private String[] basePackages;
    private String packagesToScan;
    private String persistenceUnitName;
}
