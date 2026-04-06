package org.app.database.mybatis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Per-connection LRU cache of {@link PreparedStatement} objects.
 * <p>
 * <b>Design:</b> The cache key is {@code (System.identityHashCode(connection), sql, flags)}.
 * This means:
 * <ul>
 *   <li>Statements are cached per physical connection — no cross-connection sharing</li>
 *   <li>When a pool recycles a connection, the old connection identity is gone and new
 *       statements are prepared fresh on the new connection object</li>
 *   <li>HikariCP / most pools already cache PreparedStatements at the pool level.
 *       This cache adds an extra hit for the case where pool caching is off or limited.</li>
 * </ul>
 *
 * <b>Thread safety:</b> This class is NOT thread-safe. Each call site must use its own
 * connection from the pool (which is the normal JDBC pattern — never share a Connection
 * across threads). One {@code DynSql} instance holds one {@code StmtCache} but access
 * is safe because each thread has its own connection from the pool.
 */
final class StmtCache {

    private static final int DEFAULT_MAX = 512;

    /**
     * Cache key: connection identity + sql + "keys" flag.
     * Using identityHashCode avoids holding a reference to the Connection object
     * (which would prevent pool reclamation).
     */
    private final Map<String, PreparedStatement> cache;

    StmtCache(int maxSize) {
        int cap = maxSize;
        this.cache = new LinkedHashMap<String, PreparedStatement>(cap, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, PreparedStatement> eldest) {
                if (size() > cap) {
                    closeSilently(eldest.getValue());
                    return true;
                }
                return false;
            }
        };
    }

    StmtCache() { this(DEFAULT_MAX); }

    /**
     * Get or prepare a statement for the given connection + sql.
     *
     * @param conn       active JDBC connection from pool
     * @param sql        SQL string (exactly as it will be sent to DB)
     * @param returnKeys true for INSERT statements needing auto-generated keys
     */
    PreparedStatement get(Connection conn, String sql, boolean returnKeys) throws SQLException {
        String key = buildKey(conn, sql, returnKeys);
        PreparedStatement ps = cache.get(key);
        if (ps != null) {
            // Validate — closed if connection was recycled and wrapper was retained
            try {
                if (!ps.isClosed()) return ps;
            } catch (SQLException ignored) {}
            cache.remove(key);
        }
        ps = returnKeys
            ? conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)
            : conn.prepareStatement(sql);
        cache.put(key, ps);
        return ps;
    }

    /** Convenience — no generated keys (most common path). */
    PreparedStatement get(Connection conn, String sql) throws SQLException {
        return get(conn, sql, false);
    }

    /** Evict entries for a specific connection (call when returning connection to pool). */
    void evictConnection(Connection conn) {
        String prefix = Integer.toHexString(System.identityHashCode(conn)) + ":";
        cache.entrySet().removeIf(e -> {
            if (e.getKey().startsWith(prefix)) {
                closeSilently(e.getValue());
                return true;
            }
            return false;
        });
    }

    /** Evict all cached entries for a given SQL across all connections. */
    void invalidateSql(String sql) {
        cache.entrySet().removeIf(e -> {
            // key format: "<connHash>:<sql>" or "<connHash>:<sql>:keys"
            boolean match = e.getKey().contains(":" + sql);
            if (match) closeSilently(e.getValue());
            return match;
        });
    }

    void clear() {
        cache.values().forEach(StmtCache::closeSilently);
        cache.clear();
    }

    int size() { return cache.size(); }

    // ── internal ──────────────────────────────────────────────────────────────

    private static String buildKey(Connection conn, String sql, boolean returnKeys) {
        // identityHashCode → unique per connection object lifetime
        return Integer.toHexString(System.identityHashCode(conn))
            + ":" + sql
            + (returnKeys ? ":K" : "");
    }

    private static void closeSilently(PreparedStatement ps) {
        try { if (ps != null && !ps.isClosed()) ps.close(); }
        catch (SQLException ignored) {}
    }
}
