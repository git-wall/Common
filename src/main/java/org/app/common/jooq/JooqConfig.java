package org.app.common.jooq;

import com.zaxxer.hikari.HikariDataSource;
import org.app.common.db.DataSourceUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Value("${jooq.database.type:postgres}")
    private String databaseType;

    @Bean("jooqDataSource")
    public DataSource dataSource(@Value("${jooq.url}") String url,
                                 @Value("${jooq.username:default}") String username,
                                 @Value("${jooq.password:}") String password,
                                 @Value("${jooq.driverClassname:}") String driverClassName) {
        HikariDataSource dataSource = DataSourceUtils.defaultDataSource();
        dataSource.setJdbcUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        return dataSource;
    }

    @Bean
    @ConditionalOnBean(name = {"dataSource", "DataSource", "hikariDataSource", "HikariDataSource"})
    public DefaultConfiguration jooqConfiguration(DataSource dataSource) {
        DefaultConfiguration config = new DefaultConfiguration();
        config.set(getDialect());
        config.setDataSource(dataSource);
        return config;
    }

    @Bean
    @ConditionalOnBean(name = {"dataSource", "DataSource", "hikariDataSource", "HikariDataSource"})
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(this.jooqConfiguration(dataSource));
    }

    private SQLDialect getDialect() {
        switch (databaseType.toLowerCase()) {
            case "mysql":
                return SQLDialect.MYSQL;
            case "postgres":
                return SQLDialect.POSTGRES;
            default:
                throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
    }
}
