package org.app.cache.cache2lv;

import lombok.AllArgsConstructor;
import org.app.cache.jedis.RedisClientFactory;
import org.app.cache.jedis.RedisConfig;
import org.app.cache.jedis.RedisJson;
import org.app.cache.jedis.RedisOps;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Full demo of TieredCache in all real-world scenarios.
 */
public class CacheUsageExample {

    // Dummy domain types
    @AllArgsConstructor
    static class User {
        String id;
        String name;
        String email;
    }

    @AllArgsConstructor
    static class Product {
        long id;
        String name;
        double price;
    }

    // Dummy repos
    static User findUserFromDb(String id) {
        return new User(id, "Alice", "alice@x.com");
    }

    static Product findProductFromDb(long id) {
        return new Product(id, "Laptop", 999.9);
    }

    public static void main(String[] args) throws Exception {

        // =====================================================================
        // 1. Infrastructure setup (once at startup)
        // =====================================================================

        RedisConfig redisConfig = RedisConfig.builder()
            .host("localhost").port(6379)
            .compressionEnabled(true).compressionThresholdBytes(256)
            .build();

        RedisClientFactory factory = RedisClientFactory.of(redisConfig);
        RedisOps ops = RedisOps.of(factory);
        RedisJson json = RedisJson.of(ops);

        // =====================================================================
        // 2. Build a TieredCache for Users
        // =====================================================================

        CacheConfig userCacheConfig = CacheConfig.builder()
            .ttl(Duration.ofMinutes(5))
            .refreshLeadTime(Duration.ofSeconds(30)) // background refresh 30s before expiry
            .l2Enabled(true)
            .l2Ttl(Duration.ofMinutes(30))           // L2 keeps it much longer
            .loaderRetries(2)
            .loaderRetryDelay(Duration.ofMillis(200))
            .allowStaleOnError(true)                 // degrade gracefully on DB failure
            .staleGracePeriod(Duration.ofMinutes(10))
            .localMaxSize(5_000)
            .maintenanceInterval(Duration.ofSeconds(10))
            .build();

        L2CacheProvider<String, User> userL2 =
            RedisL2Provider.of(json, ops, "users", User.class);

        TieredCache<String, User> userCache = TieredCache.<String, User>builder()
            .name("users")
            .config(userCacheConfig)
            .loader(key -> Optional.ofNullable(findUserFromDb(key)))
            .l2Provider(userL2)
            .build();

        // =====================================================================
        // 3. Build a TieredCache for Products — L1 only (no Redis)
        // =====================================================================

        TieredCache<Long, Product> productCache = TieredCache.<Long, Product>builder()
            .name("products")
            .config(CacheConfig.builder()
                .ttl(Duration.ofMinutes(10))
                .refreshLeadTime(Duration.ofMinutes(1))
                .l2Enabled(false)   // no distributed layer needed
                .loaderRetries(3)
                .build())
            .loader(id -> Optional.ofNullable(findProductFromDb(id)))
            .l2Provider(NoOpL2Provider.instance())
            .build();

        // =====================================================================
        // 4. Registry (optional, but recommended)
        // =====================================================================

        CacheRegistry registry = new CacheRegistry();

        // Global listener for all caches → plug in your metrics here
        registry.addGlobalListener(event -> System.out.printf("[METRICS] cache=%s type=%s key=%s%n",
            event.cacheName(), event.type(), event.key()));

        registry.register("users", userCache);
        registry.register("products", productCache);

        // Warmup on startup (e.g. hot keys from a config file)
        registry.warmup("users", List.of("user:1", "user:2", "user:admin"));

        // =====================================================================
        // 5. Normal reads — fully transparent to caller
        // =====================================================================

        Optional<User> u = userCache.get("user:1");
        u.ifPresent(user -> System.out.println("Got user: " + user));

        // Second call: L1 hit, no network
        userCache.get("user:1");

        // =====================================================================
        // 6. After DB write — push fresh value immediately (no wait for TTL)
        // =====================================================================

        User updatedUser = new User("user:1", "Alice Updated", "new@x.com");

        // Option A: push known value directly (no loader call)
        userCache.put("user:1", updatedUser);

        // Option B: force-reload from DB (calls loader immediately)
        userCache.refresh("user:1");

        // =====================================================================
        // 7. Explicit invalidation — remove from L1 + L2 + broadcast to peers
        // =====================================================================

        userCache.evict("user:1");

        // =====================================================================
        // 8. Cross-instance invalidation via Redis pub/sub (optional)
        //    Other JVM instances will evict "user:1" from THEIR L1 too
        // =====================================================================

        if (userL2 instanceof RedisL2Provider) {
            RedisL2Provider<String, User> redisL2 = (RedisL2Provider<String, User>) userL2;
            redisL2.subscribeInvalidations(rawKey -> {
                // This runs in a background thread on each service instance
                userCache.evict(rawKey);
                System.out.println("[PubSub] L1 evicted key=" + rawKey);
            });
        }

        // =====================================================================
        // 9. Fine-grained event listener on a specific cache
        // =====================================================================

        userCache.addListener(event -> {
            switch (event.type()) {
                case L1_HIT: { /* fast path, no-op */ }
                case MISS:
                    System.out.println("Cache miss: " + event.key());
                case STALE_SERVED:
                    System.out.println("⚠ Stale data served: " + event.key());
                case LOAD_FAILED:
                    System.err.println("✗ Load failed: " + event.key() + " - " + event.error());
                default: { /* ignore */ }
            }
        });

        // =====================================================================
        // 10. Health check endpoint (e.g. /actuator/cache or custom)
        // =====================================================================

        System.out.println("L1 sizes: " + registry.l1Sizes());
        System.out.println("Registered caches: " + registry.registeredNames());

        // =====================================================================
        // 11. Shutdown
        // =====================================================================

        registry.close(); // closes all caches + background threads
        factory.close();
    }
}
