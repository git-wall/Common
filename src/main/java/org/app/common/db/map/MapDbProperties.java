package org.app.common.db.map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties(prefix = "mapdb")
public class MapDbProperties {
    private MapType type = MapType.B_TREE; // B+Tree or HTreeMap
    private CacheProperties cache = new CacheProperties();
    private KafkaProperties kafka = new KafkaProperties();
    private LoggingProperties log = new LoggingProperties();

    public enum MapType {
        B_TREE,
        H_TREE
    }

    @Getter
    @Setter
    public static class KafkaProperties {
        private boolean enabled = false;
        private String brokerId = "localhost:9092";
        private String topic = "mapdb";
    }

    @Getter
    @Setter
    public static class CacheProperties {
        private boolean enabled = false;
        private long ttl = 3600L; // Cache TTL (in seconds)
        private Map<String, Long> tables = new HashMap<>(4);
    }

    @Getter
    @Setter
    public static class LoggingProperties {
        private boolean enabled = false;
    }
}