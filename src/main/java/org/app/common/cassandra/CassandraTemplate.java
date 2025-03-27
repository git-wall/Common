package org.app.common.cassandra;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.cassandra.core.cql.AsyncCqlTemplate;
import org.springframework.data.cassandra.core.cql.CqlTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class CassandraTemplate {
    private final CqlTemplate cqlTemplate;
    private final AsyncCqlTemplate asyncCqlTemplate;
    private final CqlSession session;

    public <T> T queryForObject(String cql, Class<T> requiredType, Object... args) {
        try {
            return cqlTemplate.queryForObject(cql, requiredType, args);
        } catch (Exception e) {
            log.error("Error executing CQL query: {}", cql, e);
            throw new CassandraException("Error executing query", "QUERY_ERROR", e);
        }
    }

    public <T> List<T> queryForList(String cql, Class<T> elementType, Object... args) {
        try {
            return cqlTemplate.queryForList(cql, elementType, args);
        } catch (Exception e) {
            log.error("Error executing CQL list query: {}", cql, e);
            throw new CassandraException("Error executing list query", "LIST_QUERY_ERROR", e);
        }
    }

    public boolean execute(String cql, Object... args) {
        try {
            cqlTemplate.execute(cql, args);
            return true;
        } catch (Exception e) {
            log.error("Error executing CQL: {}", cql, e);
            throw new CassandraException("Error executing CQL", "EXECUTE_ERROR", e);
        }
    }

    public CompletableFuture<Boolean> executeAsync(String cql, Object... args) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                asyncCqlTemplate.execute(cql, args).get();
                return true;
            } catch (Exception e) {
                log.error("Error executing async CQL: {}", cql, e);
                throw new CassandraException("Error executing async CQL", "ASYNC_ERROR", e);
            }
        });
    }

    public BatchStatement createBatch() {
        return BatchStatement.builder(DefaultBatchType.LOGGED).build();
    }

    public PreparedStatement prepare(String cql) {
        return session.prepare(cql);
    }

    public void executeBatch(BatchStatement batch) {
        try {
            session.execute(batch);
        } catch (Exception e) {
            log.error("Error executing batch", e);
            throw new CassandraException("Error executing batch", "BATCH_ERROR", e);
        }
    }

    public <T> Optional<T> queryForOptional(String cql, Class<T> requiredType, Object... args) {
        return Optional.ofNullable(queryForObject(cql, requiredType, args));
    }
}