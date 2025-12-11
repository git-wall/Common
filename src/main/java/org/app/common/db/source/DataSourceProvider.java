package org.app.common.db.source;

import org.app.common.db.source.config.DataSourceConfig;

import java.util.Map;

/**
 * Interface for loading datasource configurations from different sources
 */
public interface DataSourceProvider {
    /**
     * Load datasource configurations
     * @return Map of datasource configurations
     */
    Map<String, DataSourceConfig> loadDataSourceConfigs();
}
