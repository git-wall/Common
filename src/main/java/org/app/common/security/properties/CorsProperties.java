package org.app.common.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "app.cors.allowed")
@Getter
@Setter
public class CorsProperties {
    private String origins = "*";

    public List<String> getOrigins() {
        return Arrays.asList(origins.split(","));
    }
}
