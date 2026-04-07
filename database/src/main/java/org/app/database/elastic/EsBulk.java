package org.app.database.elastic;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Fluent bulk API for Elasticsearch 7.x.
 * <p>
 * Supports auto-flush by count threshold. Always call {@link #flush()} at the end.
 *
 * <pre>{@code
 * // Simple batch index
 * EsBulk.on(ops)
 *     .index("users", "u1", user1)
 *     .index("users", "u2", user2)
 *     .delete("users", "u3")
 *     .flush();
 *
 * // Large import with auto-flush every 1000 docs
 * EsBulk bulk = EsBulk.on(ops).autoFlushAt(1000);
 * for (User u : millionUsers) {
 *     bulk.index("users", u.id(), u);
 * }
 * BulkResult result = bulk.flush();
 * System.out.println("Failures: " + result.failures());
 * }</pre>
 */
public final class EsBulk {

    private final EsOps ops;
    private BulkRequest request = new BulkRequest();
    private int pendingCount = 0;
    private int autoFlushAt = 0;   // 0 = disabled

    private final List<BulkItemResponse> allFailures = new ArrayList<>();
    private int totalIndexed = 0;
    private int totalDeleted = 0;
    private int totalUpdated = 0;

    private EsBulk(EsOps ops) {
        this.ops = ops;
    }

    public static EsBulk on(EsOps ops) {
        return new EsBulk(ops);
    }

    /**
     * Auto-flush when pending request count reaches this threshold.
     */
    public EsBulk autoFlushAt(int count) {
        this.autoFlushAt = count;
        return this;
    }

    // -------------------------------------------------------------------------
    // Actions
    // -------------------------------------------------------------------------

    public <T> EsBulk index(String index, String id, T doc) {
        Map<String, Object> source = ops.mapper().toMap(doc);
        IndexRequest req = id != null
            ? new IndexRequest(index).id(id).source(source)
            : new IndexRequest(index).source(source);
        request.add(req);
        totalIndexed++;
        return maybeAutoFlush();
    }

    public <T> EsBulk index(String index, T doc) {
        return index(index, null, doc);
    }

    public EsBulk indexMap(String index, String id, Map<String, Object> source) {
        request.add(new IndexRequest(index).id(id).source(source));
        totalIndexed++;
        return maybeAutoFlush();
    }

    public EsBulk delete(String index, String id) {
        request.add(new DeleteRequest(index, id));
        totalDeleted++;
        return maybeAutoFlush();
    }

    /**
     * Partial update — only specified fields.
     */
    public EsBulk update(String index, String id, Map<String, Object> fields) {
        request.add(new UpdateRequest(index, id).doc(fields));
        totalUpdated++;
        return maybeAutoFlush();
    }

    public <T> EsBulk update(String index, String id, T partial) {
        return update(index, id, ops.mapper().toMap(partial));
    }

    /**
     * Upsert in bulk.
     */
    public <T> EsBulk upsert(String index, String id, T upsertDoc, Map<String, Object> updateFields) {
        UpdateRequest req = new UpdateRequest(index, id)
            .doc(updateFields)
            .upsert(ops.mapper().toMap(upsertDoc));
        request.add(req);
        totalUpdated++;
        return maybeAutoFlush();
    }

    // -------------------------------------------------------------------------
    // Execute
    // -------------------------------------------------------------------------

    /**
     * Send all pending requests to ES. Safe to call even if no pending requests.
     *
     * @return BulkResult with stats and any failures.
     */
    public BulkResult flush() {
        if (request.numberOfActions() == 0) {
            return new BulkResult(totalIndexed, totalDeleted, totalUpdated, allFailures);
        }
        try (var client = ops.client()){
            BulkResponse resp = client.bulk(request, RequestOptions.DEFAULT);
            if (resp.hasFailures()) {
                for (BulkItemResponse item : resp) {
                    if (item.isFailed()) allFailures.add(item);
                }
            }
            pendingCount = 0;
            request = new BulkRequest(); // reset
            return new BulkResult(totalIndexed, totalDeleted, totalUpdated, allFailures);
        } catch (IOException e) {
            throw new EsOps.EsException("bulk flush failed", e);
        }
    }

    public int pendingCount() {
        return request.numberOfActions();
    }

    // -------------------------------------------------------------------------

    private EsBulk maybeAutoFlush() {
        if (autoFlushAt > 0 && request.numberOfActions() >= autoFlushAt) {
            flush();
        }
        return this;
    }

    // -------------------------------------------------------------------------
    // Result
    // -------------------------------------------------------------------------
    @Data
    @AllArgsConstructor
    public static class BulkResult{
        int totalIndexed;
        int totalDeleted;
        int totalUpdated;
        List<BulkItemResponse> failures;

        public boolean hasFailures() {
            return !failures.isEmpty();
        }

        public int failureCount() {
            return failures.size();
        }

        public int total() {
            return totalIndexed + totalDeleted + totalUpdated;
        }

        @Override
        public String toString() {
            return String.format("BulkResult[indexed=%d, deleted=%d, updated=%d, failures=%d]",
                totalIndexed, totalDeleted, totalUpdated, failures.size());
        }
    }
}
