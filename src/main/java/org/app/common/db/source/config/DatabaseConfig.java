package org.app.common.db.source.config;

import lombok.RequiredArgsConstructor;
import org.app.common.db.source.DataSourceProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provider that loads datasource configurations from a database
 */
@RequiredArgsConstructor
public class DatabaseConfig implements DataSourceProvider {

    private final JdbcTemplate jdbcTemplate;

    @Value("${multi.datasource.query}")
    private String query;

    @Override
    public Map<String, DataSourceConfig> loadDataSourceConfigs() {
        Assert.notNull(query, "query find info multi datasource must not be null");

        return jdbcTemplate
            .query(query, (rs, rn) -> getSourceConfig(rs))
            .stream()
            .collect(Collectors.toMap(
                DataSourceConfig::getName,
                Function.identity()
            ));
    }

    private DataSourceConfig getSourceConfig(ResultSet rs) throws SQLException {
        DataSourceConfig config = new DataSourceConfig();
        config.setName(rs.getString("name"));
        config.setUrl(rs.getString("url"));
        config.setUsername(rs.getString("username"));
        config.setPassword(rs.getString("password"));
        config.setDriverClassName(rs.getString("driverClassName"));

        // Get additional properties if available
        try {
            config.setMaxPoolSize(rs.getInt("maxPoolSize"));
        } catch (SQLException e) {
            // Column might not exist, use default value
            config.setMaxPoolSize(10);
        }

        try {
            config.setMinIdle(rs.getInt("minIdle"));
        } catch (SQLException e) {
            // Column might not exist, use default value
            config.setMinIdle(5);
        }

        try {
            config.setConnectionTimeout(rs.getLong("connectionTimeout"));
        } catch (SQLException e) {
            // Column might not exist, use default value
            config.setConnectionTimeout(20000L);
        }

        try {
            config.setIdleTimeout(rs.getLong("idleTimeout"));
        } catch (SQLException e) {
            // Column might not exist, use default value
            config.setIdleTimeout(300000L);
        }

        return config;
    }
}
