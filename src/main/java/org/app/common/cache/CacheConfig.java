package org.app.common.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    // apply for all cache
    @Bean
    public CacheManagerCustomizer<CaffeineCacheManager> cacheCustomizer() {
        return manager -> manager.setCaffeine(
            Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .initialCapacity(200)
        );
    }

    /**
     * apply different cache spec
     * <pre>{@code
     * @Cacheable("contracts)
     * public Contract findContract(Long id) {
     *     return contractRepository.findById(id).orElseThrow();
     * }
     * }</pre>
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.registerCustomCache("contracts",
            Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .maximumSize(1000)
                .build());

        manager.registerCustomCache("reports",
            Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .maximumSize(500)
                .build());

        return manager;
    }
}
