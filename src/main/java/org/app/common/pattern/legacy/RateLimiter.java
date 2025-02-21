/** if you want to use it remove line 1 and 38
package org.app.common.pattern;


public class RateLimiter {
//    private final ConcurrentHashMap<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>(16);
//    private final int limit;
//    private final long durationInMillis;
//
//    public RateLimiter(int limit, long durationInMillis) {
//        this.limit = limit;
//        this.durationInMillis = durationInMillis;
//    }
//
//    public synchronized boolean allowRequest(String clientId) {
//        AtomicInteger count = requestCounts.computeIfAbsent(clientId, k -> new AtomicInteger(0));
//
//        if (count.get() >= limit) {
//            return false;
//        }
//
//        count.incrementAndGet();
//        scheduleReset(clientId);
//        return true;
//    }
//
//    private void scheduleReset(String clientId) {
//        new Thread(() -> {
//            try {
//                Thread.sleep(durationInMillis);
//                requestCounts.remove(clientId);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }).start();
//    }
}
 */
