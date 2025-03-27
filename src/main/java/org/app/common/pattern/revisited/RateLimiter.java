package org.app.common.pattern.revisited;

import java.util.concurrent.TimeUnit;

public class RateLimiter {
    private final long timeWindowMs;
    private final int maxRequests;
    private int currentRequests;
    private long windowStart;

    public RateLimiter(int maxRequests, long timeWindow, TimeUnit timeUnit) {
        this.maxRequests = maxRequests;
        this.timeWindowMs = timeUnit.toMillis(timeWindow);
        this.windowStart = System.currentTimeMillis();
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - windowStart >= timeWindowMs) {
            currentRequests = 0;
            windowStart = now;
        }

        if (currentRequests < maxRequests) {
            currentRequests++;
            return true;
        }
        return false;
    }
}