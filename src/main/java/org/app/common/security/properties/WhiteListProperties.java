package org.app.common.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.whitelist")
public class WhiteListProperties {
    private List<String> whiteList = new ArrayList<>();
}
