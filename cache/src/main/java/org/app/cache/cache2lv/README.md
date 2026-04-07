# TieredCache — Architecture & Design

```
┌─────────────────────────────────────────────────────────────────────┐
│                         caller: cache.get(key)                      │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │     L1: ConcurrentHashMap│  nanosecond
                    │     (per JVM instance)   │  access
                    └────────────┬────────────┘
                           HIT ──┤
                                 │ MISS
                    ┌────────────▼────────────┐
                    │   Anti-Stampede Lock     │  per-key ReentrantLock
                    │   (only 1 thread loads)  │  others wait / serve stale
                    └────────────┬────────────┘
                           HIT ──┤  double-check L1 after lock
                                 │ MISS
                    ┌────────────▼────────────┐
                    │   L2: L2CacheProvider    │  ~1ms (Redis + LZ4)
                    │   (distributed, shared)  │
                    └────────────┬────────────┘
                           HIT ──┤  warm L1 from L2
                                 │ MISS
                    ┌────────────▼────────────┐
                    │   CacheLoader (DB call)  │  retry with backoff
                    │   + retry on failure     │
                    └────────────┬────────────┘
                                 │ write L1 + L2
                    ┌────────────▼────────────┐
                    │  Return to caller        │
                    └─────────────────────────┘

── Background threads ──────────────────────────────────────────────────

  Maintenance scheduler (every N seconds):
    ├── Scan L1 entries: evict expired-beyond-grace
    ├── Schedule background refresh for entries within refreshLeadTime
    └── Enforce localMaxSize limit

  Refresh executor (unbounded cached pool):
    └── Runs loader silently → updates L1 + L2
        (caller keeps getting current value while refresh happens)

  Pub/Sub subscriber (optional, 1 daemon thread per namespace):
    └── Listens on Redis channel "__cache_invalidate__:<namespace>"
        → evicts L1 on other instances when one instance evicts

── Write-side API ──────────────────────────────────────────────────────

  cache.put(key, value)    → push known value to L1 + L2 immediately
  cache.refresh(key)       → force reload from loader → L1 + L2
  cache.evict(key)         → remove from L1 + L2 + broadcast to peers

── Resilience ──────────────────────────────────────────────────────────

  Scenario                         Behavior
  ─────────────────────────────    ────────────────────────────────────
  Loader fails                     retry N times, then serve stale
  L2 (Redis) is down               transparent fallback to loader
  All sources fail, stale exists   serve stale if within grace period
  All sources fail, no stale       throw CacheLoadException
  Many threads hit same miss key   only 1 calls loader, others wait
  Wait timeout expired             serve stale or return empty
```

## Class Map

| Class                | Role                                                     |
|----------------------|----------------------------------------------------------|
| `TieredCache`        | Core engine — L1/L2/loader orchestration                 |
| `CacheConfig`        | All tuning knobs (TTL, refresh lead, retry, stale, ...)  |
| `CacheLoader<K,V>`   | FunctionalInterface — implement to call your DB          |
| `L2CacheProvider<K,V>`| Interface — plug in Redis, Memcached, or any backend   |
| `RedisL2Provider`    | Redis impl using RedisJson + LZ4 compression             |
| `NoOpL2Provider`     | No-op impl for L1-only or testing                        |
| `CacheEntry`         | Internal value holder with expiry + refresh state        |
| `CacheEvent`         | Typed event for observability (metrics, logging)         |
| `CacheRegistry`      | Multi-cache management, warmup, global listeners         |

## Patterns

**Read-through (default)**
```java
Optional<User> user = cache.get("user:1"); // transparent
```

**Write-through (after save)**
```java
userRepo.save(user);
cache.put("user:" + user.id(), user); // push immediately, no DB round-trip
```

**Invalidate + let it lazy-reload**
```java
userRepo.delete(id);
cache.evict("user:" + id);
```

**Force reload (you know it changed)**
```java
userRepo.save(user);
cache.refresh("user:" + user.id()); // calls loader immediately
```

**Warmup on startup**
```java
registry.warmup("users", configuredHotKeys);
```
