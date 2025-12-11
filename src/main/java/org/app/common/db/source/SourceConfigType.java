package org.app.common.db.source;

import lombok.Getter;
import org.app.common.db.source.config.DatabaseConfig;
import org.app.common.db.source.config.PropertiesConfig;

/**
 * Enum defining different types of datasource configurations
 */
public enum SourceConfigType {

    /**
     * Load datasource's from application properties/YAML
     */
    PROPERTIES(PropertiesConfig.class),

    /**
     * Load datasource's from database
     */
    DATABASE(DatabaseConfig.class);

    @Getter
    private final Class<? extends DataSourceProvider> dataSourceProvider;

    SourceConfigType(Class<? extends DataSourceProvider> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }
}
