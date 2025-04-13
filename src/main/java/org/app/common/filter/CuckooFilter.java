package org.app.common.filter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Used for: Membership tests (with deletion), cache systems, network routers
/**
 * <pre>{@code
 * ✅ 1. Membership Tests (with deletion)
 * Used to check if an item (like a username, IP address, email, etc.) exists.
 *
 * Database: Load existing usernames or keys at startup.
 * Message Queues: Real-time data updates via Kafka, RabbitMQ.
 * APIs: External system feeds, e.g., spam blacklists.
 * //
 * ✅ 2. Cache Systems
 * Used to check if a key might be in cache (to avoid hitting DB).
 *
 * Redis/Memcached: Use Cuckoo Filter for front-check.
 * Database: Preload hot keys or popular queries.
 * Log Analysis: Keys frequently accessed from logs.
 * //
 * ✅ 3. Network Routers / Firewalls
 * Used for real-time IP lookup: is this IP blacklisted or not?
 *
 * Firewall rules in DB.
 * Threat intelligence APIs (like AbuseIPDB, Spamhaus).
 * Security logs with suspicious IPs.
 * }
 * */
public class CuckooFilter<T> {
    private static final int BUCKET_SIZE = 4;
    private static final int MAX_KICKS = 500;

    private final List<Set<Integer>> buckets;
    private final int capacity;

    public CuckooFilter(int capacity) {
        this.capacity = capacity;
        this.buckets = new ArrayList<>(capacity);
        for (int i = 0; i < capacity; i++) {
            buckets.add(new HashSet<>());
        }
    }

    public boolean insert(T item) {
        int fingerprint = fingerprint(item);
        int i1 = hash1(item);
        int i2 = i1 ^ hash2(fingerprint);

        if (addToBucket(i1, fingerprint) || addToBucket(i2, fingerprint)) {
            return true;
        }

        int i = Math.random() < 0.5 ? i1 : i2;
        for (int n = 0; n < MAX_KICKS; n++) {
            Set<Integer> bucket = buckets.get(i);
            Integer victim = bucket.iterator().next();
            bucket.remove(victim);
            i = i ^ hash2(victim);
            bucket = buckets.get(i);
            if (bucket.size() < BUCKET_SIZE) {
                bucket.add(victim);
                return true;
            }
        }
        return false; // Table full
    }

    public boolean contains(T item) {
        int fingerprint = fingerprint(item);
        int i1 = hash1(item);
        int i2 = i1 ^ hash2(fingerprint);
        return buckets.get(i1).contains(fingerprint) || buckets.get(i2).contains(fingerprint);
    }

    public boolean delete(T item) {
        int fingerprint = fingerprint(item);
        int i1 = hash1(item);
        int i2 = i1 ^ hash2(fingerprint);
        return buckets.get(i1).remove(fingerprint) || buckets.get(i2).remove(fingerprint);
    }

    private boolean addToBucket(int i, int fingerprint) {
        Set<Integer> bucket = buckets.get(i);
        if (bucket.size() < BUCKET_SIZE) {
            bucket.add(fingerprint);
            return true;
        }
        return false;
    }

    private int fingerprint(T item) {
        return item.hashCode() & 0xFFFF;
    }

    private int hash1(T item) {
        return Math.abs(item.hashCode()) % capacity;
    }

    private int hash2(int fingerprint) {
        return Math.abs(Integer.hashCode(fingerprint)) % capacity;
    }

    public void clear() {
        for (Set<Integer> bucket : buckets) {
            bucket.clear();
        }
    }
}
