package org.app.database.cql;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe prepared statement cache.
 * <p>
 * Prepared statements are compiled once on the server side and reused for every
 * subsequent execution. This is critical for performance — never use
 * {@code SimpleStatement} in hot paths.
 * <p>
 * This cache uses the CQL string as the key, so identical queries always
 * reuse the same {@link PreparedStatement}.
 *
 * <pre>{@code
 * CqlPrepared prepared = CqlPrepared.of(session);
 *
 * // Prepare once (or get from cache on subsequent calls)
 * PreparedStatement ps = prepared.get(
 *     "SELECT * FROM users WHERE id = ?");
 *
 * // Bind and execute
 * BoundStatement bs = ps.bind("user-123");
 * session.execute(bs);
 *
 * // Shortcut — prepare + bind in one call
 * BoundStatement bs = prepared.bind(
 *     "INSERT INTO users (id, name) VALUES (?, ?)",
 *     "user-123", "Alice");
 * session.execute(bs);
 *
 * // Clear cache (e.g. after schema migration)
 * prepared.clear();
 * }</pre>
 *
 * <b>Important:</b> Do NOT use string concatenation to inject values into CQL.
 * Always use {@code ?} placeholders and bind values — protects against CQL injection
 * and allows the driver to cache execution plans.
 */
public final class CqlPrepared {

    private final CqlSession                              session;
    private final ConcurrentHashMap<String, PreparedStatement> cache = new ConcurrentHashMap<>();

    private CqlPrepared(CqlSession session) {
        this.session = session;
    }

    public static CqlPrepared of(CqlSession session) {
        return new CqlPrepared(session);
    }

    public static CqlPrepared of(CqlSessionFactory factory) {
        return new CqlPrepared(factory.session());
    }

    // -------------------------------------------------------------------------

    /**
     * Get or prepare a statement. Thread-safe — uses computeIfAbsent.
     * The CQL string is the cache key.
     */
    public PreparedStatement get(String cql) {
        return cache.computeIfAbsent(cql, session::prepare);
    }

    /**
     * Prepare + bind positional values in one call.
     *
     * <pre>
     * BoundStatement bs = prepared.bind(
     *     "SELECT * FROM orders WHERE user_id = ? AND status = ?",
     *     userId, "PENDING");
     * </pre>
     */
    public BoundStatement bind(String cql, Object... values) {
        return get(cql).bind(values);
    }

    /**
     * Force re-prepare (useful after schema migrations that alter a table).
     */
    public PreparedStatement reprepare(String cql) {
        PreparedStatement fresh = session.prepare(cql);
        cache.put(cql, fresh);
        return fresh;
    }

    /** Remove a single statement from the cache. */
    public void invalidate(String cql) {
        cache.remove(cql);
    }

    /** Clear all cached statements. */
    public void clear() {
        cache.clear();
    }

    public int size() { return cache.size(); }
}
