package org.app.common.db.jdbc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

@Repository
@RequiredArgsConstructor
public class BaseRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    @Getter
    private final JdbcTemplate simpleJdbcTemplate;

    // === CRUD Operations ===

    public int insert(String sql, Map<String, ?> params) {
        return jdbcTemplate.update(sql, params);
    }

    public Number insertReturningId(String sql, Map<String, ?> params) {
        Number id = jdbcTemplate.queryForObject(sql, params, Number.class);
        if (id == null) {
            throw new IllegalStateException("Insert did not return an ID");
        }
        return id;
    }

    public int update(String sql, Map<String, ?> params) {
        return jdbcTemplate.update(sql, params);
    }

    public int delete(String sql, Map<String, ?> params) {
        return jdbcTemplate.update(sql, params);
    }

    // === Query Operations ===

    public <T> List<T> findAnyMatch(String sql, Map<String, ?> params, Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");
        return jdbcTemplate.query(sql, params, EntityMetadata.getRowMapper(clazz));
    }

    public <T> Optional<T> findFirst(String sql, Map<String, ?> params, Class<T> clazz) {
        Objects.requireNonNull(clazz, "Class cannot be null");

        List<T> results = jdbcTemplate.query(sql, params, EntityMetadata.getRowMapper(clazz));

        if (results.isEmpty()) {
            return Optional.empty();
        }
        if (results.size() > 1) {
            throw new IllegalStateException("Query returned more than one result");
        }
        return Optional.of(results.get(0));
    }

    public boolean exists(String sql, Map<String, ?> params) {
        Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
        return count != null && count > 0;
    }

    public <T> T findFirst(String sql, PreparedStatementCallback<T> action) {
        return simpleJdbcTemplate.execute(sql, action);
    }

    // === Batch Operations ===

    /**
     * Batch insert với batch size tùy chỉnh
     * @return int[][] - Mảng 2 chiều: [batch_index][row_affected]
     */
    public <T> int[][] insertBatch(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        return batchExec(sql, data, batchSize, setter);
    }

    /**
     * Batch update với batch size tùy chỉnh
     * @return int[][] - Mảng 2 chiều: [batch_index][row_affected]
     */
    public <T> int[][] updateBatch(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        return batchExec(sql, data, batchSize, setter);
    }

    /**
     * Batch delete với batch size tùy chỉnh
     * @return int[][] - Mảng 2 chiều: [batch_index][row_affected]
     */
    public <T> int[][] deleteBatch(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        return batchExec(sql, data, batchSize, setter);
    }

    /**
     * Execute batch operation
     * @return int[][] - [batch_index][row_affected]
     *         Ví dụ: [[1,1,1], [1,1]] nghĩa là 2 batches, batch 1 có 3 rows, batch 2 có 2 rows
     */
    private <T> int[][] batchExec(String sql, List<T> data, int batchSize, BiConsumer<PreparedStatement, T> setter) {
        Objects.requireNonNull(data, "Data cannot be null");
        Objects.requireNonNull(setter, "Setter cannot be null");

        if (data.isEmpty()) {
            return new int[0][0];
        }

        // Spring's batchUpdate internally handles batching, just call it once
        return simpleJdbcTemplate.batchUpdate(sql, data, batchSize, setter::accept);
    }
}
