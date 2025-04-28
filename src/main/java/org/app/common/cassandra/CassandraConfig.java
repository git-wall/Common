package org.app.common.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import lombok.Data;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.CqlSessionFactoryBean;
import org.springframework.data.cassandra.core.cql.AsyncCqlTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;

import java.time.Duration;

@Data
@Configuration
@EnableConfigurationProperties(CassandraProperties.class)
public class CassandraConfig {

    @Bean
    public CqlSessionFactoryBean session(CassandraProperties properties) {
        CqlSessionFactoryBean session = new CqlSessionFactoryBean();
        session.setContactPoints(properties.getContactPoints());
        session.setPort(properties.getPort());
        session.setLocalDatacenter(properties.getLocalDatacenter());
        session.setKeyspaceName(properties.getKeyspace());

        if (properties.getUsername() != null && properties.getPassword() != null) {
            session.setUsername(properties.getUsername());
            session.setPassword(properties.getPassword());
        }

        // Configure session with builder options
        session.setSessionBuilderConfigurer(builder -> {
            DriverConfigLoader loader = DriverConfigLoader.programmaticBuilder()
                .withInt(DefaultDriverOption.CONNECTION_POOL_LOCAL_SIZE, properties.getPool().getMaxConnections())
                .withInt(DefaultDriverOption.REQUEST_THROTTLER_MAX_CONCURRENT_REQUESTS, properties.getPool().getMaxRequests())
                .withDuration(DefaultDriverOption.HEARTBEAT_INTERVAL, Duration.ofSeconds(properties.getPool().getHeartbeatIntervalSeconds()))
                .build();
            builder.withConfigLoader(loader);
            return builder;
        });

        return session;
    }

    @Bean
    public CqlTemplate cqlTemplate(CqlSession session) {
        return new CqlTemplate(session);
    }

    @Bean
    public AsyncCqlTemplate asyncCqlTemplate(CqlSession session) {
        return new AsyncCqlTemplate(session);
    }
}