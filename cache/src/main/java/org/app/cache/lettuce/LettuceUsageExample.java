package org.app.cache.lettuce;

import io.lettuce.core.ScoredValue;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Complete usage demo for the Lettuce Redis utility module.
 */
public class LettuceUsageExample {

    @AllArgsConstructor
    static class User {
        String id;
        String name;
        String email;
        int age;
    }

    @AllArgsConstructor
    static class Product {
        long id;
        String name;
        double price;
    }

    static User findUserFromDb(String id) {
        return new User(id, "Alice", "alice@x.com", 30);
    }

    static Product findProductFromDb(long id) {
        return new Product(id, "Laptop", 999.9);
    }

    public static void main(String[] args) throws Exception {

        // =====================================================================
        // 1. Setup — once at startup
        // =====================================================================

        // Standalone
        LettuceConfig config = LettuceConfig.standalone()
            .host("localhost").port(6379)
            // .password("secret")
            .compressionEnabled(true)
            .compressionThresholdBytes(256)
            .build();

        // Cluster (comment in if needed)
        // LettuceConfig config = LettuceConfig.cluster()
        //     .clusterNodes(List.of("node1:7001","node2:7002","node3:7003"))
        //     .build();

        // Sentinel
        // LettuceConfig config = LettuceConfig.sentinel()
        //     .sentinelMasterId("mymaster")
        //     .sentinelNodes(List.of("s1:26379","s2:26379"))
        //     .build();

        LettuceClientFactory factory = LettuceClientFactory.of(config);
        System.out.println("Ping: " + factory.ping());

        // Layers
        LettuceOps ops = LettuceOps.of(factory);
        LettuceAsyncOps asyncOps = LettuceAsyncOps.of(factory);
        LettuceJson json = LettuceJson.of(ops);
        LettuceCache cache = LettuceCache.of(json, 300);

        // =====================================================================
        // 2. String ops (with transparent LZ4 compression)
        // =====================================================================

        ops.set("greeting", "Hello Lettuce");
        ops.set("ttl-key", "expires soon", 60);
        ops.set("nx-test", "only if new", 30);                  // set if not exists with TTL

        Optional<String> val = ops.get("greeting");
        val.ifPresent(v -> System.out.println("Got: " + v));

        // SET NX (only if not exists)
        boolean didSet = ops.setNx("lock-test", "unique-token", 30);
        System.out.println("setNx: " + didSet);

        // SET XX (only if exists)
        boolean updated = ops.setXx("greeting", "Updated", 120);
        System.out.println("setXx: " + updated);

        // GETDEL (get + delete atomically, Redis 6.2+)
        Optional<String> gotten = ops.getDel("nx-test");

        // Counters
        ops.set("views", "0");
        System.out.println("incr: " + ops.incr("views"));
        System.out.println("incrBy 5: " + ops.incrBy("views", 5));
        System.out.println("incrByFloat: " + ops.incrByFloat("views", 1.5));

        // TTL
        ops.expire("greeting", 600);
        System.out.println("TTL: " + ops.ttl("greeting"));
        ops.persist("greeting");    // remove TTL

        System.out.println("exists: " + ops.exists("greeting"));
        System.out.println("type: " + ops.type("greeting"));

        // =====================================================================
        // 3. Binary / large payload (auto LZ4)
        // =====================================================================

        byte[] bigData = new byte[10_000];
        ops.setBinary("blob", bigData, 120);
        byte[] back = ops.getBinary("blob");
        System.out.println("Binary roundtrip: " + back.length + " bytes");

        // =====================================================================
        // 4. Hash
        // =====================================================================

        ops.hset("user:1", "name", "Alice");
        ops.hset("user:1", "age", "30");
        ops.hsetAll("user:2", Map.of("name", "Bob", "email", "bob@x.com", "age", "25"));
        ops.hsetNx("user:1", "email", "alice@x.com");   // only if field missing

        System.out.println("hget: " + ops.hget("user:1", "name"));
        System.out.println("hgetAll: " + ops.hgetAll("user:1"));
        System.out.println("hmget: " + ops.hmget("user:1", "name", "age"));
        System.out.println("hkeys: " + ops.hkeys("user:1"));
        System.out.println("hlen: " + ops.hlen("user:1"));

        ops.hincrBy("user:1", "age", 1);
        ops.hdel("user:1", "age");

        // =====================================================================
        // 5. List
        // =====================================================================

        ops.rpush("queue", "job1", "job2", "job3");
        ops.lpush("queue", "job0");
        System.out.println("llen: " + ops.llen("queue"));
        System.out.println("lrange: " + ops.lrange("queue", 0, -1));
        System.out.println("lpop: " + ops.lpop("queue"));
        System.out.println("rpop: " + ops.rpop("queue"));
        ops.ltrim("queue", 0, 9);       // keep only first 10

        // =====================================================================
        // 6. Set
        // =====================================================================

        ops.sadd("tags", "redis", "cache", "nosql", "fast");
        ops.srem("tags", "slow");
        System.out.println("smembers: " + ops.smembers("tags"));
        System.out.println("sismember: " + ops.sismember("tags", "redis"));
        System.out.println("scard: " + ops.scard("tags"));

        ops.sadd("tags2", "redis", "distributed");
        System.out.println("sinter: " + ops.sinter("tags", "tags2"));
        System.out.println("sunion: " + ops.sunion("tags", "tags2"));

        // =====================================================================
        // 7. Sorted Set (leaderboard)
        // =====================================================================

        ops.zadd("leaderboard", 2300, "alice");
        ops.zadd("leaderboard", 1800, "bob");
        ops.zadd("leaderboard", 3100, "carol");
        ops.zaddNx("leaderboard", 500, "newbie");    // only if not exists
        ops.zincrby("leaderboard", 200, "bob");

        System.out.println("top 3: " + ops.zrevrange("leaderboard", 0, 2));
        System.out.println("score: " + ops.zscore("leaderboard", "alice"));
        System.out.println("rank: " + ops.zrank("leaderboard", "alice"));
        System.out.println("count 1000+: " + ops.zcount("leaderboard", 1000, Double.MAX_VALUE));

        List<ScoredValue<String>> withScores = ops.zrangeWithScores("leaderboard", 0, -1);
        withScores.forEach(sv -> System.out.printf("  %s: %.0f%n", sv.getValue(), sv.getScore()));

        // =====================================================================
        // 8. Scan keys safely (no KEYS *)
        // =====================================================================

        List<String> userKeys = ops.scanKeys("user:*");
        System.out.println("User keys: " + userKeys);
        System.out.println("Key count: " + ops.countKeys("user:*"));

        // =====================================================================
        // 9. Pipeline batch (sync, single flush)
        // =====================================================================

        ops.pipeline(cmds -> {
            cmds.set("pipe:a", "1");
            cmds.set("pipe:b", "2");
            cmds.set("pipe:c", "3");
            cmds.expire("pipe:a", 60);
            cmds.expire("pipe:b", 60);
            cmds.expire("pipe:c", 60);
        });
        System.out.println("Pipelined 6 commands in one flush");

        // =====================================================================
        // 10. Async operations
        // =====================================================================

        // Fire individual futures
        CompletableFuture<Void> f1 = asyncOps.set("async:k1", "v1").thenApply(r -> null);
        CompletableFuture<Void> f2 = asyncOps.set("async:k2", "v2").thenApply(r -> null);
        CompletableFuture.allOf(f1, f2).join();

        // Parallel gets
        CompletableFuture<Optional<String>> g1 = asyncOps.get("async:k1");
        CompletableFuture<Optional<String>> g2 = asyncOps.get("async:k2");
        CompletableFuture.allOf(g1, g2).join();
        System.out.println("Async get k1: " + g1.join());

        // Async pipeline batch
        asyncOps.batchSync(cmds -> List.of(
            cmds.set("batch:x", "10"),
            cmds.set("batch:y", "20"),
            cmds.incr("batch:x"),
            cmds.incr("batch:y")
        ));
        System.out.println("batch:x = " + asyncOps.get("batch:x").join());

        // =====================================================================
        // 11. JSON objects (Jackson + LZ4)
        // =====================================================================

        User user = new User("u1", "Alice", "alice@x.com", 30);
        json.set("user:u1", user, 600);

        Optional<User> loaded = json.get("user:u1", User.class);
        loaded.ifPresent(u -> System.out.println("JSON loaded: " + u));

        // Async JSON
        json.setAsync("user:u2", new User("u2", "Bob", "bob@x.com", 25))
            .thenRun(() -> System.out.println("async json set done"));

        CompletableFuture<Optional<User>> asyncUser = json.getAsync("user:u2", User.class);
        asyncUser.thenAccept(u -> u.ifPresent(v -> System.out.println("async json get: " + v)));

        // =====================================================================
        // 12. Cache-aside (sync + async)
        // =====================================================================

        // Sync get-or-load
        User cachedUser = cache.getOrLoad("user:u1", User.class,
            () -> findUserFromDb("u1"));
        System.out.println("Cache hit: " + cachedUser);

        // Async get-or-load
        CompletableFuture<User> asyncCached = cache.getOrLoadAsync("user:u3", User.class,
            () -> CompletableFuture.supplyAsync(() -> findUserFromDb("u3")));
        asyncCached.thenAccept(u -> System.out.println("Async cache: " + u));

        // After DB write — push directly
        User upd = new User("u1", "Alice Updated", "alice@x.com", 31);
        cache.put("user:u1", upd);
        cache.evict("user:u1");   // or invalidate

        // =====================================================================
        // 13. Distributed Lock (sync + async)
        // =====================================================================

        LettuceLock lock = LettuceLock.of(ops, "lock:payment:42", 10);

        // Sync withLock
        lock.withLock(3, 100, () -> {
            System.out.println("Processing inside lock");
        });

        // Sync withLock returning value
        String result = lock.withLock(() -> "computed inside lock");

        // Async withLock
        lock.withLockAsync(() ->
            CompletableFuture.supplyAsync(() -> "async computation")
        ).thenAccept(r -> System.out.println("Async lock result: " + r));

        // Manual
        boolean got = lock.tryLock(5, 200);
        if (got) {
            try { /* critical section */ } finally {
                lock.unlock();
            }
        }

        // =====================================================================
        // 14. Pub/Sub
        // =====================================================================

        LettucePubSub pubsub = LettucePubSub.of(factory);

        // Subscribe to channel
        pubsub.subscribe("notifications", (channel, msg) ->
            System.out.println("[" + channel + "] " + msg));

        // Pattern subscribe
        pubsub.psubscribe("events.*", (pattern, channel, msg) ->
            System.out.println("[pattern:" + pattern + "] " + channel + " -> " + msg));

        // Publish (from any instance, any thread)
        ops.publish("notifications", "User signed up!");
        ops.publish("events.login", "alice logged in");

        Thread.sleep(200); // let subscriber process

        pubsub.unsubscribe("notifications");
        pubsub.close();

        // =====================================================================
        // 15. DB-level
        // =====================================================================

        System.out.println("DB size: " + ops.dbSize());
        System.out.println("Info memory: " + ops.info().lines()
            .filter(l -> l.startsWith("used_memory:")).findFirst().orElse("n/a"));

        // =====================================================================
        // 16. Shutdown
        // =====================================================================

        factory.close();
        System.out.println("Done.");
    }
}
