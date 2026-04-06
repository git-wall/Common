package org.app.security.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.cors.allowed")
@Getter
@Setter
public class CorsProperties {
    private List<String> originPatterns;
}
