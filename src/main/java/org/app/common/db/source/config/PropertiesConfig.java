package org.app.common.db.source.config;

import org.app.common.db.source.DataSourceProvider;

import java.util.Collections;
import java.util.Map;

/**
 * Provider that loads datasource configurations from MultiDataSourceProperties
 */
public class PropertiesConfig implements DataSourceProvider {

    private final MultiDataSourceProperties properties;

    public PropertiesConfig(MultiDataSourceProperties properties) {
        this.properties = properties;
    }

    @Override
    public Map<String, DataSourceConfig> loadDataSourceConfigs() {
        if (properties != null && properties.getDataSources() != null) {
            return properties.getDataSources();
        }

        return Collections.emptyMap();
    }
}
