package org.app.common.db.map;

import lombok.RequiredArgsConstructor;
import org.app.common.context.SpringContext;
import org.app.common.kafka.multi.BrokerManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MapDbProperties.class)
@RequiredArgsConstructor
public class MapDbAutoConfiguration {

    private final MapDbProperties properties;

    @Bean
    public MemoryCache memoryCache() {
        return new MemoryCache(properties);
    }

    @Bean
    public Pipeline pipeline(MemoryCache memoryCache) {
        return new Pipeline(
                properties,
                SpringContext.getContext().getBean(BrokerManager.class),
                memoryCache
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public MapDb mapDb(Pipeline pipeline) {
        return new MapDb(properties, pipeline);
    }
}
