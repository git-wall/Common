package org.app.database.elastic;

import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.reindex.ReindexRequest;
import org.elasticsearch.xcontent.XContentType;

import java.io.IOException;
import java.util.Map;

/**
 * Index administration utilities for Elasticsearch 7.x.
 * <p>
 * Handles index lifecycle: create, delete, exists, mappings, settings, aliases, reindex.
 *
 * <pre>
 * EsIndex idx = EsIndex.on(ops);
 *
 * // Create with defaults
 * idx.create("users");
 *
 * // Create with shards/replicas + mapping from JSON string
 * idx.create("users", 3, 1, """
 *   { "properties": {
 *       "name":  { "type": "text" },
 *       "email": { "type": "keyword" },
 *       "age":   { "type": "integer" }
 *   }}
 * """);
 *
 * // Alias
 * idx.addAlias("users_v2", "users");
 *
 * // Zero-downtime reindex
 * idx.reindex("users_v1", "users_v2");
 *
 * // Update mapping (add new field)
 * idx.putMapping("users", """{ "properties": { "phone": { "type": "keyword" }}}""");
 *
 * // Delete
 * idx.delete("users_v1");
 * </pre>
 */
public final class EsIndex {

    private final EsOps ops;

    private EsIndex(EsOps ops) {
        this.ops = ops;
    }

    public static EsIndex on(EsOps ops) {
        return new EsIndex(ops);
    }

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    /**
     * Create index with default settings (1 shard, 1 replica).
     */
    public void create(String index) {
        create(index, 1, 1, null);
    }

    /**
     * Create index with explicit shards/replicas and optional mapping JSON.
     *
     * @param mappingJson ES mapping JSON for the "properties" block, or null
     */
    public void create(String index, int shards, int replicas, String mappingJson) {
        try (var client = ops.client()) {
            CreateIndexRequest req = new CreateIndexRequest(index)
                .settings(Settings.builder()
                    .put("index.number_of_shards", shards)
                    .put("index.number_of_replicas", replicas)
                    .build());
            if (mappingJson != null) {
                req.mapping(mappingJson, XContentType.JSON);
            }
            client.indices().create(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("create index failed: " + index, e);
        }
    }

    /**
     * Create index from a full settings+mappings Map.
     * Useful when loading config from YAML/JSON files.
     */
    public void create(String index, Map<String, Object> settings, Map<String, Object> mappings) {
        try (var client = ops.client()) {
            CreateIndexRequest req = new CreateIndexRequest(index);
            if (settings != null) req.settings(settings);
            if (mappings != null) req.mapping(mappings);
            client.indices().create(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("create index failed: " + index, e);
        }
    }

    /**
     * Create index only if it doesn't exist. Returns true if created.
     */
    public boolean createIfAbsent(String index) {
        if (exists(index)) return false;
        create(index);
        return true;
    }

    public boolean createIfAbsent(String index, int shards, int replicas, String mappingJson) {
        if (exists(index)) return false;
        create(index, shards, replicas, mappingJson);
        return true;
    }

    // -------------------------------------------------------------------------
    // Exists / Delete
    // -------------------------------------------------------------------------

    public boolean exists(String index) {
        try (var client = ops.client()) {
            return client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("exists check failed: " + index, e);
        }
    }

    public void delete(String index) {
        try (var client = ops.client()) {
            client.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("delete index failed: " + index, e);
        }
    }

    public void deleteIfExists(String index) {
        if (exists(index)) delete(index);
    }

    // -------------------------------------------------------------------------
    // Mapping
    // -------------------------------------------------------------------------

    /**
     * Add or update mapping fields (cannot remove fields in ES).
     *
     * @param mappingJson JSON with "properties" block
     */
    public void putMapping(String index, String mappingJson) {
        try (var client = ops.client()) {
            client.indices().putMapping(
                new PutMappingRequest(index).source(mappingJson, XContentType.JSON),
                RequestOptions.DEFAULT
            );
        } catch (IOException e) {
            throw new EsOps.EsException("putMapping failed: " + index, e);
        }
    }

    /**
     * Get current mapping for an index.
     */
    public Map<String, Object> getMapping(String index) {
        try (var client = ops.client()) {
            GetMappingsResponse resp = client.indices().getMapping(
                new GetMappingsRequest().indices(index), RequestOptions.DEFAULT);
            return resp.mappings().get(index).getSourceAsMap();
        } catch (IOException e) {
            throw new EsOps.EsException("getMapping failed: " + index, e);
        }
    }

    // -------------------------------------------------------------------------
    // Settings
    // -------------------------------------------------------------------------

    /**
     * Update dynamic index settings (e.g. number_of_replicas, refresh_interval).
     * Note: number_of_shards cannot be changed after creation.
     */
    public void putSettings(String index, Map<String, Object> settings) {
        try (var client = ops.client()) {
            client.indices().putSettings(
                new org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest(index)
                    .settings(settings),
                RequestOptions.DEFAULT
            );
        } catch (IOException e) {
            throw new EsOps.EsException("putSettings failed: " + index, e);
        }
    }

    /**
     * Useful during bulk import: disable refresh temporarily for speed.
     */
    public void disableRefresh(String index) {
        putSettings(index, Map.of("index.refresh_interval", "-1"));
    }

    public void enableRefresh(String index) {
        putSettings(index, Map.of("index.refresh_interval", "1s"));
    }

    // -------------------------------------------------------------------------
    // Aliases
    // -------------------------------------------------------------------------

    /**
     * Add an alias pointing to an index.
     * Pattern: real index = "users_v2", alias = "users"
     */
    public void addAlias(String index, String alias) {
        try (var client = ops.client()) {
            IndicesAliasesRequest req = new IndicesAliasesRequest()
                .addAliasAction(IndicesAliasesRequest.AliasActions.add()
                    .index(index).alias(alias));
            client.indices().updateAliases(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("addAlias failed: " + index + " -> " + alias, e);
        }
    }

    public void removeAlias(String index, String alias) {
        try (var client = ops.client()) {
            IndicesAliasesRequest req = new IndicesAliasesRequest()
                .addAliasAction(IndicesAliasesRequest.AliasActions.remove()
                    .index(index).alias(alias));
            client.indices().updateAliases(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("removeAlias failed: " + index + " -> " + alias, e);
        }
    }

    /**
     * Zero-downtime alias swap: atomically move alias from old to new index.
     * <pre>
     * idx.swapAlias("users_v1", "users_v2", "users");
     * // old: users → users_v1   new: users → users_v2
     * </pre>
     */
    public void swapAlias(String oldIndex, String newIndex, String alias) {
        try (var client = ops.client()) {
            IndicesAliasesRequest req = new IndicesAliasesRequest()
                .addAliasAction(IndicesAliasesRequest.AliasActions.remove()
                    .index(oldIndex).alias(alias))
                .addAliasAction(IndicesAliasesRequest.AliasActions.add()
                    .index(newIndex).alias(alias));
            client.indices().updateAliases(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("swapAlias failed: " + oldIndex + " -> " + newIndex, e);
        }
    }

    // -------------------------------------------------------------------------
    // Reindex
    // -------------------------------------------------------------------------

    /**
     * Reindex all documents from source to destination index.
     * Both indices must exist. Destination keeps existing documents.
     */
    public void reindex(String sourceIndex, String destIndex) {
        try (var client = ops.client()) {
            ReindexRequest req = new ReindexRequest()
                .setSourceIndices(sourceIndex)
                .setDestIndex(destIndex);
            client.reindex(req, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("reindex failed: " + sourceIndex + " -> " + destIndex, e);
        }
    }

    /**
     * Full zero-downtime reindex pattern:
     * 1. Create new index with new mapping
     * 2. Reindex data from old to new
     * 3. Swap alias
     * 4. (Optionally) delete old index
     */
    public void reindexWithAliasSwap(String alias, String oldIndex, String newIndex,
                                     int shards, int replicas, String newMappingJson,
                                     boolean deleteOld) {
        create(newIndex, shards, replicas, newMappingJson);
        disableRefresh(newIndex);
        reindex(oldIndex, newIndex);
        enableRefresh(newIndex);
        swapAlias(oldIndex, newIndex, alias);
        if (deleteOld) delete(oldIndex);
    }

    // -------------------------------------------------------------------------
    // Refresh / Flush
    // -------------------------------------------------------------------------

    /**
     * Force refresh so recently indexed docs are immediately searchable.
     */
    public void refresh(String... indices) {
        try (var client = ops.client()) {
            client.indices().refresh(new RefreshRequest(indices), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("refresh failed", e);
        }
    }

    /**
     * Force flush (fsync) segments to disk.
     */
    public void flush(String... indices) {
        try (var client = ops.client()) {
            client.indices().flush(new FlushRequest(indices), RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new EsOps.EsException("flush failed", e);
        }
    }
}
