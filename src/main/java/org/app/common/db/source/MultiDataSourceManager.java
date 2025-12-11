package org.app.common.db.source;

import com.zaxxer.hikari.HikariDataSource;
import org.app.common.db.DataSourceUtils;
import org.app.common.db.source.config.DataSourceConfig;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Manager for multiple datasource's
 */
public class MultiDataSourceManager implements DisposableBean {

    private final Map<String, DataSource> dataSources = new HashMap<>();
    private final Map<String, JdbcTemplate> jdbcTemplates = new HashMap<>();

    public MultiDataSourceManager(Map<String, DataSourceConfig> sourceConfig) {
        sourceConfig.forEach(this::registerDataSource);
    }

    /**
     * Register a datasource
     * @param config The datasource configuration
     */
    public void registerDataSource(String name, DataSourceConfig config) {
        HikariDataSource dataSource = DataSourceUtils.hikariDataSource(
            config.getConnectionTimeout(),
            config.getIdleTimeout(),
            name);

        dataSource.setJdbcUrl(config.getUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setDriverClassName(config.getDriverClassName());
        dataSource.setMaximumPoolSize(config.getMaxPoolSize());
        dataSource.setMinimumIdle(config.getMinIdle());

        dataSources.put(name, dataSource);
        jdbcTemplates.put(name, new JdbcTemplate(dataSource));
    }

    /**
     * Get a datasource by name
     * @param name The datasource name
     * @return The datasource or null if not found
     */
    public DataSource getDataSource(String name) {
        return dataSources.get(name);
    }

    /**
     * Get a JdbcTemplate by datasource name
     * @param name The datasource name
     * @return The JdbcTemplate or null if not found
     */
    public JdbcTemplate getJdbcTemplate(String name) {
        return jdbcTemplates.get(name);
    }

    /**
     * Get all registered datasource names
     * @return Array of datasource names
     */
    public String[] getDataSourceNames() {
        return dataSources.keySet().toArray(new String[0]);
    }

    /**
     * Close all datasource's
     */
    public void closeAll() {
        for (DataSource dataSource : dataSources.values()) {
            if (dataSource instanceof HikariDataSource) {
                ((HikariDataSource) dataSource).close();
            }
        }
        dataSources.clear();
        jdbcTemplates.clear();
    }

    @Override
    public void destroy() {
        closeAll();
    }
}
