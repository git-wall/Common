package org.app.database.mybatis;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Caller-provided function that maps one {@link ResultSet} row to an object.
 * <p>
 * <b>Zero reflection</b> — caller writes direct {@code rs.getString(...)},
 * {@code rs.getLong(...)}, etc. This is the fastest possible mapping path.
 * <p>
 * Implement once per domain class, reuse everywhere.
 *
 * <pre>
 * // Define once — store as a static field for reuse
 * static final RowMapper&lt;User&gt; USER_MAPPER = rs -&gt; new User(
 *     rs.getLong("id"),
 *     rs.getString("name"),
 *     rs.getString("email"),
 *     rs.getTimestamp("created_at").toInstant()
 * );
 *
 * // Use anywhere
 * List&lt;User&gt; users = db.select(stmt, USER_MAPPER);
 * Optional&lt;User&gt; user = db.selectOne(stmt, USER_MAPPER);
 * </pre>
 *
 * @param <T> the type this mapper produces
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Map the current row of {@code rs} to an object.
     * The cursor is already positioned — do NOT call {@code rs.next()}.
     *
     * @param rs result set positioned at the current row
     * @return mapped object (never null recommended — use Optional at call site)
     * @throws SQLException if any column access fails
     */
    T map(ResultSet rs) throws SQLException;
}
