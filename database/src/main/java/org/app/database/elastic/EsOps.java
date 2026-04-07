package org.app.database.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Core CRUD operations for Elasticsearch 7.x.
 * <p>
 * All methods are synchronous and wrap checked exceptions into unchecked {@link EsException}.
 * Use {@link EsSearch} for queries and aggregations.
 *
 * <pre>
 * EsOps ops = EsOps.of(factory);
 *
 * // Index (create or overwrite)
 * ops.index("users", "u1", new User("Alice", 30));
 *
 * // Get
 * Optional&lt;User&gt; u = ops.get("users", "u1", User.class);
 *
 * // Partial update (only specified fields)
 * ops.update("users", "u1", Map.of("age", 31));
 *
 * // Upsert (create if not exists, merge if exists)
 * ops.upsert("users", "u1", user, Map.of("age", 31));
 *
 * // Update by Painless script
 * ops.updateScript("users", "u1", "ctx._source.age += params.delta", Map.of("delta", 1));
 *
 * // Delete
 * ops.delete("users", "u1");
 *
 * // Exists
 * boolean exists = ops.exists("users", "u1");
 *
 * // Multi-get
 * List&lt;Optional&lt;User&gt;&gt; users = ops.mget("users", List.of("u1","u2"), User.class);
 * </pre>
 */
public final class EsOps {

    private final RestHighLevelClient client;
    private final EsMapper            mapper;

    private EsOps(RestHighLevelClient client, EsMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    public static EsOps of(EsClientFactory factory) {
        return new EsOps(factory.client(), new EsMapper());
    }

    public static EsOps of(EsClientFactory factory, ObjectMapper objectMapper) {
        return new EsOps(factory.client(), new EsMapper(objectMapper));
    }

    // expose for EsSearch / EsBulk / EsIndex
    RestHighLevelClient client()  { return client; }
    EsMapper            mapper()  { return mapper; }

    // -------------------------------------------------------------------------
    // Index (create / overwrite)
    // -------------------------------------------------------------------------

    /**
     * Index a document. Auto-generates ES _id if {@code id} is null.
     * @return the assigned ES _id
     */
    public <T> String index(String index, String id, T doc) {
        try {
            Map<String, Object> source = mapper.toMap(doc);
            IndexRequest req = id != null
                ? new IndexRequest(index).id(id).source(source)
                : new IndexRequest(index).source(source);
            IndexResponse resp = client.index(req, RequestOptions.DEFAULT);
            return resp.getId();
        } catch (IOException e) {
            throw new EsException("index failed: " + index + "/" + id, e);
        }
    }

    /** Index with auto-generated id. Returns the generated _id. */
    public <T> String index(String index, T doc) {
        return index(index, null, doc);
    }

    /** Index a raw Map source directly. */
    public String indexMap(String index, String id, Map<String, Object> source) {
        try {
            IndexRequest req = id != null
                ? new IndexRequest(index).id(id).source(source)
                : new IndexRequest(index).source(source);
            return client.index(req, RequestOptions.DEFAULT).getId();
        } catch (IOException e) {
            throw new EsException("indexMap failed: " + index + "/" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // Get
    // -------------------------------------------------------------------------

    public <T> Optional<T> get(String index, String id, Class<T> type) {
        try {
            GetResponse resp = client.get(new GetRequest(index, id), RequestOptions.DEFAULT);
            if (!resp.isExists()) return Optional.empty();
            return Optional.of(mapper.fromMap(resp.getSourceAsMap(), type));
        } catch (IOException e) {
            throw new EsException("get failed: " + index + "/" + id, e);
        }
    }

    /** Get raw source as Map. */
    public Optional<Map<String, Object>> getMap(String index, String id) {
        try {
            GetResponse resp = client.get(new GetRequest(index, id), RequestOptions.DEFAULT);
            return resp.isExists() ? Optional.of(resp.getSourceAsMap()) : Optional.empty();
        } catch (IOException e) {
            throw new EsException("getMap failed: " + index + "/" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // Multi-get
    // -------------------------------------------------------------------------

    public <T> List<Optional<T>> mget(String index, List<String> ids, Class<T> type) {
        try {
            MultiGetRequest req = new MultiGetRequest();
            ids.forEach(id -> req.add(index, id));
            MultiGetResponse resp = client.mget(req, RequestOptions.DEFAULT);

            List<Optional<T>> results = new ArrayList<>();
            for (var item : resp.getResponses()) {
                if (item.isFailed() || !item.getResponse().isExists()) {
                    results.add(Optional.empty());
                } else {
                    results.add(Optional.of(mapper.fromMap(item.getResponse().getSourceAsMap(), type)));
                }
            }
            return results;
        } catch (IOException e) {
            throw new EsException("mget failed: " + index, e);
        }
    }

    // -------------------------------------------------------------------------
    // Exists
    // -------------------------------------------------------------------------

    public boolean exists(String index, String id) {
        try {
            return client.exists(new GetRequest(index, id).fetchSourceContext(
                org.elasticsearch.search.fetch.subphase.FetchSourceContext.DO_NOT_FETCH_SOURCE
            ), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("exists failed: " + index + "/" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    /** Partial update — only specified fields, others untouched. */
    public void update(String index, String id, Map<String, Object> fields) {
        try {
            client.update(new UpdateRequest(index, id).doc(fields), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("update failed: " + index + "/" + id, e);
        }
    }

    /** Partial update from a typed object (all non-null fields). */
    public <T> void update(String index, String id, T partial) {
        update(index, id, mapper.toMap(partial));
    }

    /**
     * Upsert: update if exists, insert {@code upsertDoc} if not.
     * @param fields       partial update fields (applied when doc exists)
     * @param upsertDoc    full doc to insert when doc does not exist
     */
    public <T> void upsert(String index, String id, T upsertDoc, Map<String, Object> fields) {
        try {
            UpdateRequest req = new UpdateRequest(index, id)
                .doc(fields)
                .upsert(mapper.toMap(upsertDoc));
            client.update(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("upsert failed: " + index + "/" + id, e);
        }
    }

    /**
     * Update by Painless script.
     * <pre>
     * ops.updateScript("orders", "o1", "ctx._source.status = params.s", Map.of("s", "PAID"));
     * </pre>
     */
    public void updateScript(String index, String id, String painlessScript, Map<String, Object> params) {
        try {
            Script script = new Script(ScriptType.INLINE, "painless", painlessScript, params);
            client.update(new UpdateRequest(index, id).script(script), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsException("updateScript failed: " + index + "/" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // Delete
    // -------------------------------------------------------------------------

    /** @return true if document was found and deleted */
    public boolean delete(String index, String id) {
        try {
            DeleteResponse resp = client.delete(new DeleteRequest(index, id), RequestOptions.DEFAULT);
            return resp.status() == RestStatus.OK;
        } catch (IOException e) {
            throw new EsException("delete failed: " + index + "/" + id, e);
        }
    }

    // -------------------------------------------------------------------------
    // Exception
    // -------------------------------------------------------------------------

    public static final class EsException extends RuntimeException {
        private static final long serialVersionUID = 7038703349070977506L;

        public EsException(String msg, Throwable cause) { super(msg, cause); }
        public EsException(String msg)                  { super(msg); }
    }
}
