package org.app.cache.jedis;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Full usage example — copy & run as a main class.
 */
public class  RedisUsageExample {

    public static void main(String[] args) {

        // 1. Config (khởi tạo 1 lần, dùng mãi)
        RedisConfig config = RedisConfig.builder()
            .host("localhost").port(6379)
            // .password("secret")          // bỏ comment nếu có auth
            .database(0)
            .maxTotal(32).maxIdle(8).minIdle(2)
            .compressionEnabled(true)
            .compressionThresholdBytes(512)  // chỉ nén nếu payload >= 512 bytes
            .build();

        // 2. Factory (quản lý pool — close() khi shutdown)
        RedisClientFactory factory = RedisClientFactory.of(config);

        // 3. Ops layers
        RedisOps  ops   = RedisOps.of(factory);
        RedisJson json  = RedisJson.of(ops);
        RedisCache cache = RedisCache.of(json, 300); // TTL mặc định 5 phút

        // -----------------------------------------------------------------------
        // String ops
        // -----------------------------------------------------------------------
        ops.set("greeting", "Hello Redis");
        ops.set("greeting-ttl", "expires soon", 60);

        Optional<String> val = ops.get("greeting");
        val.ifPresent(System.out::println); // Hello Redis

        ops.incr("counter");
        ops.incrBy("counter", 5);

        // -----------------------------------------------------------------------
        // Binary / large payload — auto LZ4 compressed
        // -----------------------------------------------------------------------
        byte[] bigPayload = new byte[10_000]; // pretend it's JSON / protobuf
        ops.setBinary("big:data", bigPayload, 120);
        byte[] back = ops.getBinary("big:data");
        System.out.println("Got " + back.length + " bytes back");

        // -----------------------------------------------------------------------
        // Hash
        // -----------------------------------------------------------------------
        ops.hset("user:1", "name", "Alice");
        ops.hset("user:1", "email", "alice@example.com");
        ops.hsetAll("user:2", Map.of("name", "Bob", "role", "admin"));

        Map<String, String> user = ops.hgetAll("user:1");
        System.out.println("User: " + user);

        // -----------------------------------------------------------------------
        // JSON objects (serialized + compressed automatically)
        // -----------------------------------------------------------------------
        @AllArgsConstructor
        class Product {
            long id; String name; double price;
        }

        Product p = new Product(1L, "Laptop", 999.99);
        json.set("product:1", p);
        json.set("product:1", p, 600);

        Optional<Product> loaded = json.get("product:1", Product.class);
        loaded.ifPresent(x -> System.out.println("Product: " + x));

        // -----------------------------------------------------------------------
        // Cache-aside
        // -----------------------------------------------------------------------
        Product fromCache = cache.getOrLoad(
            "product:2",
            Product.class,
            () -> new Product(2L, "Mouse", 29.99) // loader (e.g. DB call)
        );
        System.out.println("From cache: " + fromCache);

        // -----------------------------------------------------------------------
        // List
        // -----------------------------------------------------------------------
        ops.rpush("queue", "job1", "job2", "job3");
        ops.lpop("queue").ifPresent(j -> System.out.println("Processed: " + j));

        // -----------------------------------------------------------------------
        // Sorted Set (leaderboard)
        // -----------------------------------------------------------------------
        ops.zadd("leaderboard", 1500, "alice");
        ops.zadd("leaderboard", 2300, "bob");
        ops.zadd("leaderboard", 900,  "carol");

        List<String> top = ops.zrevrange("leaderboard", 0, 2);
        System.out.println("Top players: " + top); // [bob, alice, carol]

        // -----------------------------------------------------------------------
        // Distributed Lock
        // -----------------------------------------------------------------------
        RedisLock lock = RedisLock.of(ops, "lock:payment:42", 10);

        lock.withLock(3, 100, () -> {
            System.out.println("Processing payment inside lock");
            // ... critical section
        });

        // -----------------------------------------------------------------------
        // Pipeline batch
        // -----------------------------------------------------------------------
        ops.pipeline(p2 -> {
            p2.set("batch:a", "1");
            p2.set("batch:b", "2");
            p2.expire("batch:a", 60);
            p2.expire("batch:b", 60);
        });

        // -----------------------------------------------------------------------
        // Scan keys safely (no KEYS *)
        // -----------------------------------------------------------------------
        List<String> keys = ops.scanKeys("user:*");
        System.out.println("User keys: " + keys);

        // -----------------------------------------------------------------------
        // Cleanup
        // -----------------------------------------------------------------------
        System.out.println("Pool: " + factory.poolStats());
        System.out.println("Ping: " + ops.ping());
        factory.close(); // close on app shutdown
    }
}
