package org.app.common.db.source.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(value = "databases")
public class MultiDataSourceProperties {
    private Map<String, DataSourceConfig> dataSources;
}
