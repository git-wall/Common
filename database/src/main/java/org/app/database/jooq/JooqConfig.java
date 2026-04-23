package org.app.database.jooq;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.app.database.utils.DataSourceUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class JooqConfig {

    private final Environment env;

    @Bean("jooqDataSource")
    public DataSource dataSource() {
        HikariDataSource dataSource = DataSourceUtils.defaultDataSource();
        dataSource.setJdbcUrl(env.getProperty("jooq.url"));
        dataSource.setUsername(env.getProperty("jooq.username"));
        dataSource.setPassword(env.getProperty("jooq.password"));
        dataSource.setDriverClassName(env.getProperty("jooq.driverClassName"));
        return dataSource;
    }

    @Bean
    public DefaultConfiguration jooqConfiguration(DataSource dataSource) {
        var db = SQLDialect.valueOf(env.getProperty("jooq.dialect"));

        DefaultConfiguration config = new DefaultConfiguration();
        config.set(db);
        config.setDataSource(dataSource);
        return config;
    }

    @Bean
    public DSLContext dslContext(DataSource dataSource) {
        return DSL.using(this.jooqConfiguration(dataSource));
    }
}
