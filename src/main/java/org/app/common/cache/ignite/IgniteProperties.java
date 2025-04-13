package org.app.common.cache.ignite;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for Apache Ignite Spring Boot integration.
 * These properties can be configured in application.properties or application.yml.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "ignite")
public class IgniteProperties {

    // Getters and setters for main class
    /**
     * Name of the Ignite instance/cluster
     */
    private String instanceName = "ignite-cluster";

    /**
     * List of addresses for initial discovery of Ignite nodes
     */
    private List<String> discoveryAddresses = new ArrayList<>();

    /**
     * Configuration for distributed caches
     */
    private Map<String, CacheConfig> caches = new HashMap<>();

    /**
     * Configuration for persistence features
     */
    private Persistence persistence = new Persistence();

    /**
     * Configuration for compute grid features
     */
    private Compute compute = new Compute();

    /**
     * Configuration for machine learning features
     */
    private MachineLearning machineLearning = new MachineLearning();

    /**
     * Configuration for service grid (microservices)
     */
    private ServiceGrid serviceGrid = new ServiceGrid();

    // Nested configuration classes

    @Setter
    @Getter
    public static class CacheConfig {
        // Getters and setters
        private int backups = 1;
        private String cacheMode = "PARTITIONED"; // PARTITIONED, REPLICATED, LOCAL
        private String atomicityMode = "ATOMIC"; // ATOMIC, TRANSACTIONAL
        private String writeSync = "PRIMARY_SYNC"; // FULL_SYNC, PRIMARY_SYNC, FULL_ASYNC
        private boolean readThrough = false;
        private boolean writeThrough = false;
        private String storeFactory = "";

    }

    @Setter
    @Getter
    public static class Persistence {
        // Getters and setters
        private boolean enabled = false;
        private String storagePath = "${user.home}/ignite/persistence";
        private long checkpointingFrequency = 180000L; // 3 minutes in milliseconds
        private long walFlushFrequency = 2000L; // 2 seconds in milliseconds

    }

    @Setter
    @Getter
    public static class Compute {
        // Getters and setters
        private int parallelJobsNumber = 4; // Number of parallel jobs per node
        private long failoverTimeout = 60000L; // 1 minute in milliseconds

    }

    @Setter
    @Getter
    public static class MachineLearning {
        // Getters and setters
        private boolean enabled = false;
        private int threadPoolSize = 4;

    }

    @Setter
    @Getter
    public static class ServiceGrid {
        // Getters and setters
        private boolean enabled = false;
        private long serviceCallTimeout = 10000L; // 10 seconds in milliseconds

    }

}