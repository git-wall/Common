package org.app.common.action.dos;

import com.clearspring.analytics.stream.frequency.CountMinSketch;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DosDetection {

    private CountMinSketch sketch;

    @Value("${dos.cms.epsilon:0.001}")     // error factor ε
    private double eps;

    @Value("${dos.cms.delta:0.99}")        // confidence 1−δ
    private double delta;

    @Value("${dos.cms.seed:123456}")       // hash seed
    private int seed;

    @Value("${dos.threshold:10000}")       // requests threshold
    private long threshold;

    public DosDetection(CmsSeedProvider seedProvider,
                               @Value("${dos.cms.epsilon:0.001}") double eps,
                               @Value("${dos.cms.delta:0.99}") double delta) {
        int seed = seedProvider.getSeed();
        this.sketch = new CountMinSketch(eps, delta, seed);
    }

    /**
     * Record an event (e.g. a request from this IP).
     */
    public void add(String key) {
        sketch.add(key, 1L);
    }

    /**
     * Estimate how many times `key` has been seen.
     */
    public long estimateCount(String key) {
        return sketch.estimateCount(key);
    }

    /**
     * Check if `key` has exceeded the DoS threshold.
     */
    public boolean isSuspectedDos(String key) {
        return estimateCount(key) > threshold;
    }

    /**
     * Scheduled reset of the sketch (e.g. every midnight UTC).
     * This avoids unbounded growth and allows sliding‐window style detection.
     */
    @Scheduled(cron = "${dos.cms.reset-cron:0 0 0 * * ?}")
    public void resetSketch() {
        // reinitialize to clear counts
        this.sketch = new CountMinSketch(eps, delta, seed);
    }
}
