package org.app.common.scylla;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ScyllaProperties.class)
@ConditionalOnProperty(value = "scylla.enabled", havingValue = "true")
public class ScyllaConfig {

    private final ScyllaProperties properties;

    @Bean
    public CqlSession cqlSession() {
        return CqlSession.builder()
                .addContactPoints(properties.getContactPoints().stream()
                        .map(host -> new InetSocketAddress(host, properties.getPort()))
                        .collect(Collectors.toList()))
                .withLocalDatacenter(properties.getLocalDatacenter())
                .withKeyspace(properties.getKeyspace())
                .withAuthCredentials(properties.getUsername(), properties.getPassword())
                .withConfigLoader(createConfigLoader())
                .build();
    }

    private DriverConfigLoader createConfigLoader() {
        return DriverConfigLoader.programmaticBuilder()
                .withDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT, 
                        Duration.ofMillis(properties.getSocketConnectTimeout()))
                .withDuration(DefaultDriverOption.REQUEST_TIMEOUT, 
                        Duration.ofMillis(properties.getRequestTimeout()))
                .withInt(DefaultDriverOption.CONNECTION_MAX_REQUESTS, 
                        properties.getMaxRequestsPerConnection())
                .withInt(DefaultDriverOption.CONNECTION_POOL_LOCAL_SIZE, 
                        properties.getPoolingMaxConnectionsPerHost())
                .build();
    }
}