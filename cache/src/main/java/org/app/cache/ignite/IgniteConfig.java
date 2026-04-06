package org.app.cache.ignite;

import lombok.RequiredArgsConstructor;
import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;

/**
 * Autoconfiguration for Apache Ignite that sets up all necessary components
 * based on the provided configuration and annotation parameters.
 */
@RequiredArgsConstructor
public class IgniteConfig {

    private final IgniteProperties properties;

    /**
     * Configures and starts the Apache Ignite instance with all the specified features.
     */
    public Ignite igniteInstance() {
        IgniteConfiguration cfg = new IgniteConfiguration();

        // Set instance name
        cfg.setIgniteInstanceName(properties.getInstanceName());

        // Configure discovery SPI
        configureTcpDiscovery(cfg);

        // Configure persistence if enabled
        if (properties.getPersistence().isEnabled()) {
            configureDataStorage(cfg);
        }

        // Configure compute grid settings
        cfg.setPublicThreadPoolSize(properties.getCompute().getParallelJobsNumber());
        cfg.setFailureDetectionTimeout(properties.getCompute().getFailoverTimeout());

        // Start and return Ignite instance
        return Ignition.start(cfg);
    }

    /**
     * Configures TCP discovery for Ignite nodes.
     */
    private void configureTcpDiscovery(IgniteConfiguration cfg) {
        TcpDiscoverySpi discoverySpi = new TcpDiscoverySpi();

        if (properties.getDiscoveryAddresses().isEmpty()) {
            // Use multicast discovery if no specific addresses provided
            TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
            ipFinder.setMulticastGroup("228.10.10.157");
            discoverySpi.setIpFinder(ipFinder);
        } else {
            // Use static IP discovery
            TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
            ipFinder.setAddresses(properties.getDiscoveryAddresses());
            discoverySpi.setIpFinder(ipFinder);
        }

        cfg.setDiscoverySpi(discoverySpi);
    }

    /**
     * Configures data storage with persistence.
     */
    private void configureDataStorage(IgniteConfiguration cfg) {
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();

        // Configure persistent storage path
        storageCfg.getDefaultDataRegionConfiguration()
                .setPersistenceEnabled(true);

        storageCfg.setStoragePath(properties.getPersistence().getStoragePath());
        storageCfg.setCheckpointFrequency(properties.getPersistence().getCheckpointingFrequency());
        storageCfg.setWalFlushFrequency(properties.getPersistence().getWalFlushFrequency());

        cfg.setDataStorageConfiguration(storageCfg);
    }

    /**
     * OLTP Transaction service bean.
     */
    public TransactionService transactionService(Ignite ignite) {
        return new TransactionService(ignite);
    }

    /**
     * Analytics service for HTAP workloads.
     */
    public AnalyticsService analyticsService(Ignite ignite) {
        return new AnalyticsService(ignite);
    }

    /**
     * Distributed caching service.
     */
    public CacheService cacheService(Ignite ignite) {
        return new CacheService(ignite, properties.getCaches());
    }

    /**
     * Microservices data fabric manager.
     */
    public ServiceGridManager serviceGridManager(Ignite ignite) {
        return new ServiceGridManager(ignite, properties.getServiceGrid());
    }
}
