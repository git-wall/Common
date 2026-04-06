package org.app.cache.redission;

import lombok.AllArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RScript;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Complete usage demo for the Redisson utility module.
 * Notes:
 * ─────
 * • Redisson bundles its own Jackson (shaded) — no jackson-databind dep needed
 * unless you use it elsewhere.
 * <p>
 * • Default codec is JsonJacksonCodec (human-readable, debugging friendly).
 * For production performance, consider switching codec in RedissonFactory:
 * <p>
 * Config c = new Config();
 * c.setCodec(new org.redisson.codec.MsgPackJacksonCodec()); // faster + smaller
 * // or
 * c.setCodec(new org.redisson.codec.Kryo5Codec());          // fastest, non-JSON
 * <p>
 * • Redisson Watchdog: locks without explicit leaseTime auto-renew every 10s
 * (configurable via Config.lockWatchdogTimeout). Safe for long operations.
 * <p>
 * • RReliableTopic requires Redis Streams (Redis 5.0+).
 * <p>
 * • RDelayedQueue uses Sorted Sets + polling — does NOT use Redis keyspace
 * notifications. No special Redis config needed.
 * <p>
 * • For Spring integration, use redisson-spring-boot-starter instead:
 * <artifactId>redisson-spring-boot-starter</artifactId>
 * (but that's Spring — not the goal here).
 */

public class RedissonUsageExample {

    @AllArgsConstructor
    static class User {
        String id;
        String name;
        String email;
        int age;
    }

    @AllArgsConstructor
    static class Order {
        String id;
        String userId;
        double amount;
        String status;
    }

    @AllArgsConstructor
    static class UserEvent {
        String userId;
        String type;
    }

    static User findUserFromDb(String id) {
        return new User(id, "Alice", "alice@x.com", 30);
    }

    static Order findOrderFromDb(String id) {
        return new Order(id, "u1", 99.9, "PENDING");
    }

    public static void main(String[] args) throws Exception {

        // =====================================================================
        // 1. Setup — once at startup
        // =====================================================================

        RedissonConfig config = RedissonConfig.standalone()
            .address("redis://localhost:6379")
            // .password("secret")
            .database(0)
            .connectionPoolSize(64)
            .connectionMinimumIdle(10)
            .retryAttempts(3)
            .build();

        // Cluster:
        // RedissonConfig config = RedissonConfig.cluster()
        //     .nodeAddresses(List.of("redis://n1:7001","redis://n2:7002","redis://n3:7003"))
        //     .build();

        // Sentinel:
        // RedissonConfig config = RedissonConfig.sentinel()
        //     .masterName("mymaster")
        //     .sentinelAddresses(List.of("redis://s1:26379","redis://s2:26379"))
        //     .build();

        RedissonFactory factory = RedissonFactory.of(config);
        System.out.println("Ping: " + factory.ping());

        // Utility layers
        RedissonOps ops = RedissonOps.of(factory);
        RedissonCollections col = RedissonCollections.of(factory);
        RedissonLock locks = RedissonLock.of(factory);
        RedissonPubSub pubsub = RedissonPubSub.of(factory);
        RedissonRateLimit rateLimit = RedissonRateLimit.of(factory);

        // =====================================================================
        // 2. Bucket (key-value) ops — typed, codec handles serialization
        // =====================================================================

        // Store any object — Jackson serializes automatically
        User user = new User("u1", "Alice", "alice@x.com", 30);
        ops.set("user:u1", user);
        ops.set("user:u1", user, Duration.ofMinutes(10));

        // SET NX / XX
        boolean stored = ops.setNx("user:u2", new User("u2", "Bob", "b@x.com", 25));
        boolean updated = ops.setXx("user:u1", new User("u1", "Alice Updated", "a@x.com", 31));
        System.out.println("setNx: " + stored + " setXx: " + updated);

        // Get
        Optional<User> loaded = ops.get("user:u1", User.class);
        loaded.ifPresent(u -> System.out.println("Got: " + u));

        // Atomic get-and-set
        Optional<User> old = ops.getAndSet("user:u1", new User("u1", "Alice v2", "a@x.com", 32));
        old.ifPresent(u -> System.out.println("Old: " + u));

        // CAS
        User expected = new User("u1", "Alice v2", "a@x.com", 32);
        User newVal = new User("u1", "Alice v3", "a@x.com", 33);
        boolean swapped = ops.compareAndSet("user:u1", expected, newVal);
        System.out.println("CAS swapped: " + swapped);

        // GETDEL
        Optional<User> deleted = ops.getDel("user:u2");

        // TTL / expire
        ops.expire("user:u1", Duration.ofHours(1));
        ops.persist("user:u1");
        System.out.println("TTL: " + ops.ttlSeconds("user:u1") + "s");
        System.out.println("Exists: " + ops.exists("user:u1"));

        // =====================================================================
        // 3. Atomic counters
        // =====================================================================

        ops.setCounter("page:views", 0);
        System.out.println("incr: " + ops.incr("page:views"));
        System.out.println("incrBy 5: " + ops.incrBy("page:views", 5));
        System.out.println("decrBy 2: " + ops.decrBy("page:views", 2));
        System.out.println("counter: " + ops.getCounter("page:views"));
        System.out.println("float incr: " + ops.incrByFloat("ratio", 0.5));

        // =====================================================================
        // 4. Multi-get (single round-trip via RBuckets)
        // =====================================================================

        ops.set("user:a", new User("a", "Ann", "ann@x.com", 20));
        ops.set("user:b", new User("b", "Ben", "ben@x.com", 22));
        Map<String, User> users = ops.mget(User.class, "user:a", "user:b", "user:missing");
        System.out.println("mget: " + users);

        // Multi-set (single round-trip)
        ops.mset(Map.of(
            "user:c", new User("c", "Carl", "carl@x.com", 27),
            "user:d", new User("d", "Dana", "dana@x.com", 29)
        ));

        // =====================================================================
        // 5. Batch (RBatch — pipeline)
        // =====================================================================

        List<Object> results = ops.batch(batch -> {
            batch.getBucket("batch:k1").setAsync("v1");
            batch.getBucket("batch:k2").setAsync("v2");
            batch.getAtomicLong("batch:counter").incrementAndGetAsync();
            batch.getAtomicLong("batch:counter").addAndGetAsync(5);
        });
        System.out.println("Batch results: " + results);

        // =====================================================================
        // 6. Async ops
        // =====================================================================

        CompletableFuture<Void> f1 = ops.setAsync("async:k1", user);
        CompletableFuture<Void> f2 = ops.setAsync("async:k2", user, Duration.ofMinutes(5));
        CompletableFuture.allOf(f1, f2).join();

        CompletableFuture<Optional<User>> af = ops.getAsync("async:k1");
        af.thenAccept(u -> u.ifPresent(v -> System.out.println("Async get: " + v)));

        // =====================================================================
        // 7. Distributed Map (RMap)
        // =====================================================================

        col.mapPut("profile:u1", "name", "Alice");
        col.mapPut("profile:u1", "email", "alice@x.com");
        col.mapPutAll("profile:u1", Map.of("age", "30", "role", "admin"));
        col.mapPutIfAbsent("profile:u1", "score", "100");

        System.out.println("mapGet: " + col.mapGet("profile:u1", "name"));
        System.out.println("mapGetAll: " + col.mapGetAll("profile:u1"));
        System.out.println("mapSize: " + col.mapSize("profile:u1"));
        System.out.println("mapKeys: " + col.mapKeys("profile:u1"));

        col.mapRemove("profile:u1", "score");

        // MapCache — per-entry TTL
        col.mapCachePut("sessions", "sess:abc", user, Duration.ofMinutes(30));
        col.mapCachePut("sessions", "sess:def", user, Duration.ofMinutes(30), Duration.ofMinutes(10));
        System.out.println("session: " + col.mapCacheGet("sessions", "sess:abc"));
        col.mapCacheSetMaxSize("sessions", 10_000);

        // =====================================================================
        // 8. Distributed List
        // =====================================================================

        col.listAdd("todos", "task1", "task2", "task3");
        col.listAddAt("todos", 0, "urgent");
        System.out.println("list: " + col.listGetAll("todos"));
        System.out.println("list[0]: " + col.listGet("todos", 0));
        System.out.println("listSize: " + col.listSize("todos"));

        // =====================================================================
        // 9. Distributed Set
        // =====================================================================

        col.setAdd("interests", "redis", "java", "distributed");
        col.setAdd("interests2", "redis", "kotlin", "cloud");
        System.out.println("set: " + col.setGetAll("interests"));
        System.out.println("contains: " + col.setContains("interests", "redis"));
        System.out.println("intersect: " + col.setIntersect("interests", "interests2"));
        System.out.println("union: " + col.setUnion("interests", "interests2"));
        System.out.println("diff: " + col.setDiff("interests", "interests2"));

        // =====================================================================
        // 10. Sorted Set (leaderboard)
        // =====================================================================

        col.zsetAdd("leaderboard", 2300, "alice");
        col.zsetAdd("leaderboard", 1800, "bob");
        col.zsetAdd("leaderboard", 3100, "carol");
        col.zsetIncrBy("leaderboard", 200, "bob");

        System.out.println("top3: " + col.zsetTopN("leaderboard", 3));
        System.out.println("score: " + col.zsetScore("leaderboard", "alice"));
        System.out.println("rank: " + col.zsetRank("leaderboard", "alice"));
        col.zsetTopNWithScores("leaderboard", 3)
            .forEach(e -> System.out.printf("  %s: %.0f%n", e.getValue(), e.getScore()));

        // =====================================================================
        // 11. Queue
        // =====================================================================

        col.queuePush("jobs", new Order("o1", "u1", 50.0, "NEW"));
        col.queuePush("jobs", new Order("o2", "u2", 75.0, "NEW"));
        System.out.println("queue size: " + col.queueSize("jobs"));
        System.out.println("poll: " + col.queuePoll("jobs"));
        System.out.println("blocking poll: " + col.queuePollBlocking("jobs", Duration.ofSeconds(1)));

        // Deque
        col.dequePushFirst("tasks", "urgent-task");
        col.dequePushLast("tasks", "normal-task");
        System.out.println("deque first: " + col.dequePopFirst("tasks"));

        // =====================================================================
        // 12. Locks
        // =====================================================================

        // Simple lock (watchdog — no expiry while running)
        locks.withLock("payment:42", () -> {
            System.out.println("Processing payment");
        });

        // Lock returning value
        String receipt = locks.withLock("order:1", () -> "RECEIPT-12345");
        System.out.println("Receipt: " + receipt);

        // Lock with retry
        locks.withLock("resource", 5, Duration.ofMillis(200), () -> {
            System.out.println("Got resource after retry");
        });

        // Fair lock (FIFO)
        locks.withFairLock("printer", () -> System.out.println("Printing (fair order)"));

        // Read/Write lock
        locks.withReadLock("config", () -> System.out.println("Reading config"));
        locks.withWriteLock("config", () -> System.out.println("Writing config"));

        // Multi-lock (atomic across multiple keys — no deadlock)
        locks.withMultiLock(() -> System.out.println("Transfer between accounts"),
            "account:1", "account:2");

        // Semaphore (max 5 concurrent)
        locks.withSemaphore("db-connections", 5, () -> System.out.println("DB call"));

        // CountDownLatch
        locks.countDownLatchSet("startup-gate", 3);
        locks.countDown("startup-gate");
        locks.countDown("startup-gate");
        locks.countDown("startup-gate");
        locks.awaitLatch("startup-gate");
        System.out.println("All services ready!");

        // Async lock
        locks.withLockAsync("key", () ->
            CompletableFuture.supplyAsync(() -> "async result")
        ).thenAccept(r -> System.out.println("Async lock result: " + r));

        // =====================================================================
        // 13. Pub/Sub — typed, no manual JSON
        // =====================================================================

        // Standard topic
        pubsub.subscribe("user-events", UserEvent.class, (channel, event) ->
            System.out.println("[topic] " + channel + ": " + event));

        // Pattern topic
        pubsub.psubscribe("events.*", UserEvent.class, (pattern, channel, event) ->
            System.out.println("[pattern:" + pattern + "] " + channel + ": " + event));

        // Reliable topic (at-least-once delivery, survives downtime)
        pubsub.subscribeReliable("critical", UserEvent.class, (channel, event) ->
            System.out.println("[reliable] " + event));

        // Publish typed objects — no serialization needed
        pubsub.publish("user-events", new UserEvent("alice", "LOGIN"));
        pubsub.publishAsync("events.signup", new UserEvent("bob", "SIGNUP"));
        pubsub.publishReliable("critical", new UserEvent("system", "RESTART"));

        System.out.println("Subscribers on user-events: " + pubsub.subscriberCount("user-events"));
        Thread.sleep(200);
        pubsub.unsubscribe("user-events");
        pubsub.close();

        // =====================================================================
        // 14. Rate Limiting
        // =====================================================================

        // Define: 100 requests per minute, globally across all instances
        rateLimit.define("api:global", 100, Duration.ofMinutes(1));

        // Per-user: 10 req/sec
        rateLimit.define("api:user:alice", 10, Duration.ofSeconds(1));

        // Non-blocking check
        boolean allowed = rateLimit.tryAcquire("api:global");
        System.out.println("Rate limit allowed: " + allowed);

        // Wrap action — throws if exceeded
        try {
            rateLimit.withRateLimit("api:user:alice", () ->
                System.out.println("API call executed"));
        } catch (RedissonRateLimit.RateLimitExceededException e) {
            System.out.println("Rate limited: " + e.getMessage());
        }

        // Acquire with timeout
        boolean ok = rateLimit.tryAcquire("api:global", Duration.ofMillis(500));
        System.out.println("Acquired with timeout: " + ok);

        // Async rate limit
        rateLimit.withRateLimitAsync("api:global",
            () -> CompletableFuture.supplyAsync(() -> "response")
        ).thenAccept(r -> System.out.println("Async rate limited: " + r));

        // Inspect
        RedissonRateLimit.RateLimitInfo info = rateLimit.info("api:global");
        System.out.println("Rate limit info: " + info);

        // =====================================================================
        // 15. RMapCache as application cache
        // =====================================================================

        RedissonCache<User> userCache = RedissonCache.of(factory, "user-cache",
            Duration.ofMinutes(10), Duration.ofMinutes(2), 5_000);

        // Cache-aside
        User cu = userCache.getOrLoad("user:u1", User.class, () -> findUserFromDb("u1"));
        System.out.println("Cache: " + cu);

        // Async cache-aside
        userCache.getOrLoadAsync("user:u2", () ->
            CompletableFuture.supplyAsync(() -> findUserFromDb("u2"))
        ).thenAccept(u -> System.out.println("Async cache: " + u));

        // Push fresh value
        userCache.put("user:u1", new User("u1", "Alice Updated", "a@x.com", 31));
        userCache.put("user:u1", new User("u1", "Alice Short", "a@x.com", 31), Duration.ofMinutes(1));
        userCache.evict("user:u1");

        // Eviction events
        userCache.onEvict((key, value, cause) ->
            System.out.printf("Evicted [%s] key=%s%n", cause, key));
        userCache.onCreated((key, value) -> System.out.println("Created: " + key));

        System.out.println("Cache size: " + userCache.size());

        // =====================================================================
        // 16. Lua scripting
        // =====================================================================

        Long counterVal = ops.eval(
            "",
            "return redis.call('incr', KEYS[1])",
            RScript.ReturnType.LONG,
            List.of("lua:counter")
        );
        System.out.println("Lua incr result: " + counterVal);

        // =====================================================================
        // 17. Key scan
        // =====================================================================

        List<String> userKeys = ops.scanKeys("user:*");
        System.out.println("User keys: " + userKeys);
        System.out.println("DB size: " + ops.dbSize());

        // =====================================================================
        // 18. HyperLogLog (distinct count)
        // =====================================================================

        col.hllAdd("unique-visitors", "alice", "bob", "carol", "alice");
        System.out.println("Distinct visitors: " + col.hllCount("unique-visitors")); // ~3

        // =====================================================================
        // 19. Delayed Queue (schedule tasks)
        // =====================================================================

        RBlockingQueue<Order> blockingQueue = col.blockingQueue("delayed-jobs");
        RDelayedQueue<Order> delayed = col.delayedQueue("delayed-jobs");
        delayed.offer(new Order("o3", "u1", 100.0, "NEW"), 30, java.util.concurrent.TimeUnit.SECONDS);
        System.out.println("Delayed job scheduled for 30s");

        // =====================================================================
        // 20. Shutdown
        // =====================================================================

        factory.close();
        System.out.println("Done.");
    }
}
