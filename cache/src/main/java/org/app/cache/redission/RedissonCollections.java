package org.app.cache.redission;

import org.redisson.api.*;
import org.redisson.client.protocol.ScoredEntry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Distributed collection operations via Redisson.
 * <p>
 * All collections are backed by Redis and fully distributed — any JVM can
 * access the same collection by name. Types are handled by the configured codec.
 *
 * <pre>{@code
 * RedissonCollections col = RedissonCollections.of(factory);
 *
 * // Map (RMap) — works like java.util.Map but distributed
 * col.mapPut("user:1", "name", "Alice");
 * String name = col.mapGet("user:1", "name", String.class);
 * Map&lt;String, String&gt; all = col.mapGetAll("user:1", String.class);
 *
 * // Map with per-entry TTL (RMapCache)
 * col.mapCachePut("sessions", sessionId, session, Duration.ofMinutes(30));
 *
 * // List (RList) — distributed ArrayList
 * col.listAdd("mylist", "item1", "item2");
 * List&lt;String&gt; items = col.listGetAll("mylist", String.class);
 *
 * // Set (RSet) — distributed HashSet
 * col.setAdd("tags", "java", "redis");
 * Set&lt;String&gt; tags = col.setGetAll("tags", String.class);
 *
 * // Sorted Set (RScoredSortedSet) — like Redis ZADD
 * col.zsetAdd("leaderboard", 1500.0, "alice");
 * Collection&lt;String&gt; top = col.zsetTopN("leaderboard", 10, String.class);
 *
 * // Queue (RQueue) — distributed FIFO
 * col.queuePush("jobs", job);
 * Optional&lt;Job&gt; next = col.queuePoll("jobs", Job.class);
 *
 * // Deque (RDeque) — push/pop both ends
 * col.dequePushFirst("tasks", task);
 * col.dequePushLast("tasks", task);
 * }</pre>
 */
public final class RedissonCollections {

    private final RedissonClient client;

    private RedissonCollections(RedissonClient client) {
        this.client = client;
    }

    public static RedissonCollections of(RedissonFactory factory) {
        return new RedissonCollections(factory.client());
    }

    // =========================================================================
    // Map (RMap) — distributed HashMap, no per-entry TTL
    // =========================================================================

    public <V> void mapPut(String name, String field, V value) {
        client.<String, V>getMap(name).put(field, value);
    }

    public <V> void mapPutAll(String name, Map<String, V> entries) {
        client.<String, V>getMap(name).putAll(entries);
    }

    /** putIfAbsent — only stores if field doesn't exist. Returns null if stored, old value if not. */
    public <V> V mapPutIfAbsent(String name, String field, V value) {
        return client.<String, V>getMap(name).putIfAbsent(field, value);
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> mapGet(String name, String field) {
        V val = (V) client.getMap(name).get(field);
        return Optional.ofNullable(val);
    }

    public Map<Object, Object> mapGetAll(String name) {
        return client.getMap(name).readAllMap();
    }

    public boolean mapRemove(String name, String field) {
        return client.getMap(name).fastRemove(field) > 0;
    }

    public boolean mapContains(String name, String field) {
        return client.getMap(name).containsKey(field);
    }

    public int mapSize(String name) {
        return client.getMap(name).size();
    }

    public Set<Object> mapKeys(String name) {
        return client.getMap(name).keySet();
    }

    /** Get the underlying RMap for advanced operations. */
    public <K, V> RMap<K, V> rmap(String name) {
        return client.getMap(name);
    }

    // =========================================================================
    // MapCache (RMapCache) — per-entry TTL + max size eviction
    // =========================================================================

    /** Put with per-entry TTL. Existing entries' TTL is reset. */
    public <V> void mapCachePut(String name, String field, V value, Duration ttl) {
        client.<String, V>getMapCache(name).put(field, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
    }

    /** Put with idle TTL (evicts if not accessed within idleTtl). */
    public <V> void mapCachePut(String name, String field, V value, Duration ttl, Duration idleTtl) {
        client.<String, V>getMapCache(name).put(field, value,
            ttl.toMillis(), TimeUnit.MILLISECONDS,
            idleTtl.toMillis(), TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> mapCacheGet(String name, String field) {
        V val = (V) client.getMapCache(name).get(field);
        return Optional.ofNullable(val);
    }

    public boolean mapCacheRemove(String name, String field) {
        return client.getMapCache(name).fastRemove(field) > 0;
    }

    /** Set max entries. Oldest entries evicted when limit reached. */
    public void mapCacheSetMaxSize(String name, int maxSize) {
        client.getMapCache(name).setMaxSize(maxSize);
    }

    public <K, V> RMapCache<K, V> rmapCache(String name) {
        return client.getMapCache(name);
    }

    // =========================================================================
    // List (RList) — distributed ArrayList
    // =========================================================================

    @SafeVarargs
    public final <V> void listAdd(String name, V... values) {
        RList<V> list = client.getList(name);
        list.addAll(Arrays.asList(values));
    }

    public <V> void listAddAt(String name, int index, V value) {
        client.<V>getList(name).add(index, value);
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> listGet(String name, int index) {
        V val = (V) client.getList(name).get(index);
        return Optional.ofNullable(val);
    }

    @SuppressWarnings("unchecked")
    public <V> List<V> listGetAll(String name) {
        return (List<V>) client.getList(name).readAll();
    }

    @SuppressWarnings("unchecked")
    public <V> List<V> listRange(String name, int from, int to) {
        return (List<V>) client.getList(name).subList(from, to);
    }

    public boolean listRemove(String name, Object value) {
        return client.getList(name).remove(value);
    }

    public int listSize(String name) { return client.getList(name).size(); }

    public <V> RList<V> rlist(String name) { return client.getList(name); }

    // =========================================================================
    // Set (RSet) — distributed HashSet
    // =========================================================================

    @SafeVarargs
    public final <V> boolean setAdd(String name, V... members) {
        return client.<V>getSet(name).addAll(Arrays.asList(members));
    }

    @SafeVarargs
    public final <V> boolean setRemove(String name, V... members) {
        return client.<V>getSet(name).removeAll(Arrays.asList(members));
    }

    public <V> boolean setContains(String name, V member) {
        return client.<V>getSet(name).contains(member);
    }

    @SuppressWarnings("unchecked")
    public <V> Set<V> setGetAll(String name) {
        return (Set<V>) client.getSet(name).readAll();
    }

    public int setSize(String name) { return client.getSet(name).size(); }

    /** Set intersection — returns members present in ALL given sets. */
    @SuppressWarnings("unchecked")
    public <V> Set<V> setIntersect(String name, String... otherNames) {
        return (Set<V>) client.getSet(name).readIntersection(otherNames);
    }

    /** Set union — all members from all sets. */
    @SuppressWarnings("unchecked")
    public <V> Set<V> setUnion(String name, String... otherNames) {
        return (Set<V>) client.getSet(name).readUnion(otherNames);
    }

    /** Set difference — members in {@code name} not in others. */
    @SuppressWarnings("unchecked")
    public <V> Set<V> setDiff(String name, String... otherNames) {
        return (Set<V>) client.getSet(name).readDiff(otherNames);
    }

    public <V> RSet<V> rset(String name) { return client.getSet(name); }

    // =========================================================================
    // ScoredSortedSet (RScoredSortedSet) — like Redis ZSet
    // =========================================================================

    public <V> boolean zsetAdd(String name, double score, V member) {
        return client.<V>getScoredSortedSet(name).add(score, member);
    }

    public <V> void zsetAddAll(String name, Map<V, Double> membersWithScores) {
        client.<V>getScoredSortedSet(name).addAll(membersWithScores);
    }

    public <V> Double zsetScore(String name, V member) {
        return client.<V>getScoredSortedSet(name).getScore(member);
    }

    public <V> Integer zsetRank(String name, V member) {
        return client.<V>getScoredSortedSet(name).rank(member);
    }

    public <V> Integer zsetRevRank(String name, V member) {
        return client.<V>getScoredSortedSet(name).revRank(member);
    }

    /** Get top N members by score descending. */
    @SuppressWarnings("unchecked")
    public <V> Collection<V> zsetTopN(String name, int n) {
        return (Collection<V>) client.getScoredSortedSet(name).valueRangeReversed(0, n - 1);
    }

    /** Get members with scores, top N descending. */
    public <V> Collection<ScoredEntry<Object>> zsetTopNWithScores(String name, int n) {
        return client.getScoredSortedSet(name).entryRangeReversed(0, n - 1);
    }

    /** Get members in score range [min, max]. */
    @SuppressWarnings("unchecked")
    public <V> Collection<V> zsetRangeByScore(String name, double min, double max) {
        return (Collection<V>) client.getScoredSortedSet(name).valueRange(min, true, max, true);
    }

    public <V> boolean zsetRemove(String name, V member) {
        return client.<V>getScoredSortedSet(name).remove(member);
    }

    public int zsetSize(String name) { return client.getScoredSortedSet(name).size(); }

    public <V> Double zsetIncrBy(String name, double delta, V member) {
        return client.<V>getScoredSortedSet(name).addScore(member, delta);
    }

    public <V> RScoredSortedSet<V> rzset(String name) { return client.getScoredSortedSet(name); }

    // =========================================================================
    // Queue (RQueue) — distributed FIFO
    // =========================================================================

    public <V> boolean queuePush(String name, V value) {
        return client.<V>getQueue(name).offer(value);
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> queuePoll(String name) {
        V val = (V) client.getQueue(name).poll();
        return Optional.ofNullable(val);
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> queuePeek(String name) {
        V val = (V) client.getQueue(name).peek();
        return Optional.ofNullable(val);
    }

    /** Blocking poll — waits up to timeout for an item. */
    @SuppressWarnings("unchecked")
    public <V> Optional<V> queuePollBlocking(String name, Duration timeout) {
        try {
            V val = (V) client.<V>getBlockingQueue(name).poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            return Optional.ofNullable(val);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
    }

    public int queueSize(String name) { return client.getQueue(name).size(); }

    public <V> RQueue<V> rqueue(String name) { return client.getQueue(name); }

    // =========================================================================
    // Deque (RDeque) — distributed double-ended queue
    // =========================================================================

    public <V> void dequePushFirst(String name, V value) { client.<V>getDeque(name).addFirst(value); }
    public <V> void dequePushLast(String name, V value)  { client.<V>getDeque(name).addLast(value); }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> dequePopFirst(String name) {
        V val = (V) client.getDeque(name).pollFirst();
        return Optional.ofNullable(val);
    }

    @SuppressWarnings("unchecked")
    public <V> Optional<V> dequePopLast(String name) {
        V val = (V) client.getDeque(name).pollLast();
        return Optional.ofNullable(val);
    }

    public int dequeSize(String name) { return client.getDeque(name).size(); }

    public <V> RDeque<V> rdeque(String name) { return client.getDeque(name); }

    // =========================================================================
    // HyperLogLog (RHyperLogLog) — probabilistic cardinality
    // =========================================================================

    @SafeVarargs
    public final <V> boolean hllAdd(String name, V... elements) {
        return client.<V>getHyperLogLog(name).addAll(Arrays.asList(elements));
    }

    public long hllCount(String name) {
        return client.getHyperLogLog(name).count();
    }

    // =========================================================================
    // Raw access — escape hatch for any Redisson distributed object
    // =========================================================================

    public <V> RBucket<V>             bucket(String name)  { return client.getBucket(name); }
    public <V> RBlockingQueue<V>      blockingQueue(String name) { return client.getBlockingQueue(name); }
    public <V> RDelayedQueue<V>       delayedQueue(String name)  { return client.getDelayedQueue(client.getBlockingQueue(name)); }
}
