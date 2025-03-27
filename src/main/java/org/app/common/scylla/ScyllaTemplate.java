package org.app.common.scylla;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScyllaTemplate {
    
    private final CqlSession session;

    public <T> T execute(String cql, Object... values) {
        ResultSet rs = session.execute(SimpleStatement.newInstance(cql, values));
        Row row = rs.one();
        return row != null ? (T) row.getObject(0) : null;
    }

    public <T> List<T> executeQuery(String cql, Class<T> type, Object... values) {
        ResultSet rs = session.execute(SimpleStatement.newInstance(cql, values));
        return rs.all().stream()
                .map(row -> mapRowToType(row, type))
                .collect(Collectors.toList());
    }

    public CompletableFuture<AsyncResultSet> executeAsync(String cql, Object... values) {
        return session.executeAsync(SimpleStatement.newInstance(cql, values))
                .toCompletableFuture();
    }

    public <T> Optional<T> executeOptional(String cql, Class<T> resultType, Object... params) {
        return Optional.ofNullable(execute(cql, resultType, params));
    }

    public void executeVoid(String cql, Object... params) {
        try {
            session.execute(createBoundStatement(cql, params));
        } catch (Exception e) {
            log.error("Error executing void CQL: {}", cql, e);
            throw new ScyllaException("Error executing void CQL", e);
        }
    }

    public BatchStatement createBatch() {
        return BatchStatement.newInstance(BatchType.LOGGED);
    }

    public void executeBatch(BatchStatement batch) {
        session.execute(batch);
    }

    public PreparedStatement prepare(String cql) {
        return session.prepare(cql);
    }

    private BoundStatement createBoundStatement(String cql, Object... params) {
        PreparedStatement preparedStatement = session.prepare(cql);
        return preparedStatement.bind(params);
    }

    public BoundStatement bind(PreparedStatement stmt, Object... values) {
        return stmt.bind(values);
    }

    private <T> T mapRowToType(Row row, Class<T> type) {
        // Implement mapping logic based on your needs
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            for (ColumnDefinition column : row.getColumnDefinitions()) {
                String fieldName = column.getName().asInternal();
                try {
                    var field = type.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(instance, row.getObject(fieldName));
                } catch (NoSuchFieldException e) {
                    // Skip fields that don't exist in the class
                }
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping row to type: " + type, e);
        }
    }
}