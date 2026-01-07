package org.app.common.module.benchmark.aggregate;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.benchmark.core.TraceNode;
import org.app.common.module.benchmark.export.JsonExporter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public final class ProfileAggregator {

    private static final Map<String, AggNode> CACHE = new ConcurrentHashMap<>();

    private static final Map<String, Lock> LOCKS = new ConcurrentHashMap<>();

    @Setter
    private static Path baseDir = Paths.get("profiles");

    private ProfileAggregator() {}

    public static void accept(String apiName, TraceNode root) {
        if (root == null) {
            return;
        }

        Lock lock = LOCKS.computeIfAbsent(apiName, k -> new ReentrantLock());

        lock.lock();
        try {
            AggNode agg = CACHE.computeIfAbsent(apiName, api -> {
                AggNode node = new AggNode();
                node.method = api;
                return node;
            });

            merge(agg, root);

            log.debug("Aggregated call #{} for: {}", agg.stat.count, apiName);

        } finally {
            lock.unlock();
        }
    }

    public static void flush() {
        log.info("Flushing {} API profiles to disk...", CACHE.size());

        for (Map.Entry<String, AggNode> entry : CACHE.entrySet()) {
            String apiName = entry.getKey();
            AggNode agg = entry.getValue();

            Lock lock = LOCKS.get(apiName);
            if (lock != null) {
                lock.lock();
            }

            try {
                flushApi(apiName, agg);
            } catch (Exception e) {
                log.error("Failed to flush API: {}", apiName, e);
            } finally {
                if (lock != null) {
                    lock.unlock();
                }
            }
        }

        CACHE.clear();
        LOCKS.clear();

        log.info("Flush completed");
    }

    private static void flushApi(String apiName, AggNode agg) throws Exception {
        Path apiDir = baseDir.resolve(apiName);
        Path verDir = VersionManager.nextVersion(apiDir);

        log.info("Exporting {} (count={}) to {}",
            apiName, agg.stat.count, verDir);

        JsonExporter.export(agg, verDir);
    }

    private static void merge(AggNode agg, TraceNode trace) {
        agg.stat.merge(trace.timeMs, trace.memKb);

        for (TraceNode childTrace : trace.children) {

            AggNode childAgg = agg.children.computeIfAbsent(
                childTrace.method,
                method -> {
                    AggNode newChild = new AggNode();
                    newChild.method = method;
                    return newChild;
                }
            );

            merge(childAgg, childTrace);
        }
    }

    public static Map<String, AggNode> getCache() {
        return new ConcurrentHashMap<>(CACHE);
    }
}
