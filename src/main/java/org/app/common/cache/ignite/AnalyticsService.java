package org.app.common.cache.ignite;

import lombok.Getter;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.cache.query.ContinuousQuery;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.stream.StreamTransformer;

import javax.cache.Cache;
import javax.cache.event.CacheEntryEventFilter;
import javax.cache.event.CacheEntryUpdatedListener;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Service for real-time analytics and Hybrid Transactional/Analytical Processing (HTAP)
 * capabilities using Apache Ignite.
 */
@Getter
public class AnalyticsService {

    /**
     * -- GETTER --
     *  Gets the underlying Ignite instance.
     *
     */
    private final Ignite ignite;

    public AnalyticsService(Ignite ignite) {
        this.ignite = ignite;
    }

    /**
     * Creates a data streamer for high-throughput data loading into Ignite cache.
     *
     * @param cacheName The name of the cache to stream data into
     * @param <K> The key type
     * @param <V> The value type
     * @return The data streamer instance
     */
    public <K, V> IgniteDataStreamer<K, V> createDataStreamer(String cacheName) {
        return ignite.dataStreamer(cacheName);
    }

    /**
     * Streams data into Ignite cache with high throughput.
     *
     * @param cacheName The name of the cache
     * @param data The data to stream
     * @param batchSize The batch size for streaming
     * @param allowOverwrite Whether to allow overwriting existing entries
     * @param <K> The key type
     * @param <V> The value type
     */
    public <K, V> void streamData(
        String cacheName,
        Map<K, V> data,
        int batchSize,
        boolean allowOverwrite) {

        try (IgniteDataStreamer<K, V> streamer = createDataStreamer(cacheName)) {
            streamer.allowOverwrite(allowOverwrite);
            streamer.perNodeBufferSize(batchSize);

            streamer.addData(data);
            streamer.flush();
        }
    }

    /**
     * Registers a stream transformer for processing data as it's being streamed.
     *
     * @param cacheName The name of the cache
     * @param transformer The transformer to apply to streamed data
     * @param <K> The key type
     * @param <V> The value type
     */
    public <K, V> void registerStreamTransformer(
        String cacheName,
        StreamTransformer<K, V> transformer) {

        try (IgniteDataStreamer<K, V> streamer = createDataStreamer(cacheName)) {
            streamer.receiver(transformer);
        }
    }

    /**
     * Executes a SQL query for analytics purposes.
     *
     * @param query The SQL query string
     * @param args The query arguments
     * @return The query results
     */
    public List<List<?>> executeSqlQuery(String query, Object... args) {
        return executeSqlQuery("default", query, args);
    }

    /**
     * Executes a SQL query for analytics purposes on a specific cache.
     *
     * @param cacheName The name of the cache to query
     * @param query The SQL query string
     * @param args The query arguments
     * @return The query results
     */
    public List<List<?>> executeSqlQuery(String cacheName, String query, Object... args) {
        SqlFieldsQuery sqlQuery = new SqlFieldsQuery(query);

        if (args != null && args.length > 0) {
            sqlQuery.setArgs(args);
        }

        // Set to local query execution for analytics workloads
        sqlQuery.setLocal(false);

        // Enable distributed joins for complex analytics queries
        sqlQuery.setDistributedJoins(true);

        // Set query timeout
        sqlQuery.setTimeout(60000, TimeUnit.MILLISECONDS); // 1-minute timeout

        IgniteCache<?, ?> cache = ignite.cache(cacheName);
        return cache.query(sqlQuery).getAll();
    }

    /**
     * Executes a distributed ScanQuery with a filter predicate.
     *
     * @param cacheName The name of the cache to query
     * @param filter The filter predicate
     * @param <K> The key type
     * @param <V> The value type
     * @return The query cursor
     */
    public <K, V> QueryCursor<Cache.Entry<K, V>> executeScanQuery(
        String cacheName,
        IgniteBiPredicate<K, V> filter) {

        IgniteCache<K, V> cache = ignite.cache(cacheName);
        ScanQuery<K, V> scanQuery = new ScanQuery<>(filter);

        return cache.query(scanQuery);
    }

    /**
     * Sets up continuous query for real-time analytics on cache updates.
     *
     * @param cacheName The name of the cache to monitor
     * @param listener The listener to be notified of updates
     * @param filter Optional filter to apply to events
     * @param <K> The key type
     * @param <V> The value type
     * @return The registered ContinuousQuery object
     */
    public <K, V> ContinuousQuery<K, V> setupContinuousQuery(
        String cacheName,
        CacheEntryUpdatedListener<K, V> listener,
        CacheEntryEventFilter<K, V> filter) {

        IgniteCache<K, V> cache = ignite.cache(cacheName);
        ContinuousQuery<K, V> query = new ContinuousQuery<>();

        // Set the listener
        query.setLocalListener(listener);

        // Set the optional filter
        if (filter != null) {
            query.setRemoteFilterFactory(() -> filter);
        }

        // Execute the continuous query
        cache.query(query);

        return query;
    }

    /**
     * Creates a cache with analytics-optimized configuration.
     *
     * @param cacheName The name of the analytics cache
     * @param <K> The key type
     * @param <V> The value type
     * @return The configured analytics cache
     */
    public <K, V> IgniteCache<K, V> createAnalyticsCache(String cacheName) {
        CacheConfiguration<K, V> cacheCfg = new CacheConfiguration<>(cacheName);

        // Configure for analytics workload
        cacheCfg.setStatisticsEnabled(true);
        cacheCfg.setQueryDetailMetricsSize(100);
        cacheCfg.setQueryParallelism(4);

        return ignite.getOrCreateCache(cacheCfg);
    }

    /**
     * Processes each entry in a cache in parallel using Ignite's compute grid.
     *
     * @param cacheName The name of the cache to process
     * @param processor The processor function to apply to each entry
     * @param <K> The key type
     * @param <V> The value type
     */
    public <K, V> void processDataInParallel(String cacheName, Consumer<Cache.Entry<K, V>> processor) {
        IgniteCache<K, V> cache = ignite.cache(cacheName);

        ignite.compute().run(() -> {
            ScanQuery<K, V> scanQuery = new ScanQuery<>();
            scanQuery.setLocal(true);

            cache.query(scanQuery).forEach(processor);
        });
    }

}
