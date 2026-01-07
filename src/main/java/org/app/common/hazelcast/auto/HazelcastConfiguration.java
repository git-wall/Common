package org.app.common.hazelcast.auto;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <pre>{@code
 * | Lỗi                               | Nguyên nhân                     |
 * | --------------------------------- | ------------------------------- |
 * | Node không join                   | Port 5701 bị chặn               |
 * | Join nhưng tách cluster           | `cluster-name` khác             |
 * | Eureka thấy nhưng Hazelcast không | metadata `hazelcast-port` thiếu |
 * | Docker chạy 1 node                | Container NAT chặn nội bộ       |
 * }
 * </pre>
 * */
@Configuration
public class HazelcastConfiguration {

    private final String serviceName;
    private final String clusterName;

    public HazelcastConfiguration(@Value("${spring.application.name}") String serviceName,
                                  @Value("${hazelcast.cluster-name}") String clusterName) {
        this.serviceName = serviceName;
        this.clusterName = clusterName;
    }

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setClusterName(clusterName);

        // ===== Network =====
        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701).setPortAutoIncrement(true);

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig().setEnabled(false);

        // ===== Eureka Discovery =====
        EurekaConfig eureka = join.getEurekaConfig();
        eureka.setEnabled(true);
        eureka.setProperty("service-name", serviceName);
        eureka.setProperty("self-registration", "true");
        eureka.setProperty("use-metadata-for-host-and-port", "true");

        // ===== Campaign Cache =====
        MapConfig map = new MapConfig("campaign-cache");
        map.setBackupCount(1);

        NearCacheConfig nearCache = new NearCacheConfig();
        nearCache.setInvalidateOnChange(true);
        nearCache.setInMemoryFormat(InMemoryFormat.OBJECT);

        map.setNearCacheConfig(nearCache);
        config.addMapConfig(map);

        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}
