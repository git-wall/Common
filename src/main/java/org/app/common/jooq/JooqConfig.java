package org.app.common.jooq;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {

    @Bean
    @ConditionalOnProperty(name = "jooq.enabled", havingValue = "true")
    @ConditionalOnBean(name = {"dataSource", "DataSource", "hikariDataSource", "HikariDataSource"})
    public DefaultConfiguration jooqConfiguration(DataSource dataSource) {
        DefaultConfiguration config = new DefaultConfiguration();
        config.set(SQLDialect.POSTGRES);
        config.setDataSource(dataSource);
        return config;
    }

    @Bean
    @ConditionalOnProperty(name = "jooq.enabled", havingValue = "true")
    @ConditionalOnBean(name = {"dataSource", "DataSource", "hikariDataSource", "HikariDataSource"})
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(this.jooqConfiguration(dataSource));
    }
}
