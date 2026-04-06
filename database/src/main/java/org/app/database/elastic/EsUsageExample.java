package org.app.database.elastic;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.elasticsearch.index.query.QueryBuilders;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Complete usage demo for the ES 7.x utility module.
 * Every feature in one file — copy, adjust, run.
 */
public class EsUsageExample {

    // Domain types (Jackson-serializable)
    @Data
    @AllArgsConstructor
    static class User {
        String id;
        String name;
        String email;
        int age;
        String status;
    }

    @Data
    @AllArgsConstructor
    class Order {
        String id;
        String userId;
        double amount;
        String status;
        String createdAt;
    }

    public static void main(String[] args) throws Exception {

        // =====================================================================
        // 1. Setup (once at startup)
        // =====================================================================

        EsConfig config = EsConfig.builder()
            .hosts(List.of("localhost:9200"))
            .username("elastic")
            .password("changeme")
            .connectTimeoutMs(5_000)
            .socketTimeoutMs(30_000)
            .build();

        EsClientFactory factory = EsClientFactory.of(config);
        System.out.println("ES ping: " + factory.ping());

        EsOps ops = EsOps.of(factory);
        EsIndex idx = EsIndex.on(ops);

        // =====================================================================
        // 2. Index management
        // =====================================================================

        idx.deleteIfExists("users");
        idx.create("users", 1, 1, ""
//            "
//                {
//                  "properties": {
//                    "name":   { "type": "text",    "fields": { "keyword": { "type": "keyword" } } },
//                    "email":  { "type": "keyword" },
//                    "age":    { "type": "integer" },
//                    "status": { "type": "keyword" }
//                  }
//                }
//            ""
    );

        // Add a new field later
        idx.putMapping(
            "users",""
//            """{"properties": {"phone": {"type": "keyword"}}}"""
        );

        System.out.println("Index exists: " + idx.exists("users"));

        // =====================================================================
        // 3. CRUD Operations
        // =====================================================================

        // Index with specific id
        ops.index("users", "u1", new User("u1", "Alice", "alice@x.com", 30, "ACTIVE"));
        ops.index("users", "u2", new User("u2", "Bob", "bob@x.com", 25, "ACTIVE"));
        ops.index("users", "u3", new User("u3", "Charlie", "charlie@x.com", 35, "INACTIVE"));
        ops.index("users", "u4", new User("u4", "Diana", "diana@x.com", 28, "ACTIVE"));

        // Auto-generated id
        String generatedId = ops.index("users", new User(null, "Eve", "eve@x.com", 22, "ACTIVE"));
        System.out.println("Generated id: " + generatedId);

        // Get
        Optional<User> alice = ops.get("users", "u1", User.class);
        alice.ifPresent(u -> System.out.println("Got: " + u));

        // Get as raw map
        Optional<Map<String, Object>> raw = ops.getMap("users", "u1");

        // Multi-get
        List<Optional<User>> multi = ops.mget("users", List.of("u1", "u2", "u99"), User.class);
        System.out.println("mget results: " + multi.size() + " (last is empty=" + multi.get(2).isEmpty() + ")");

        // Exists
        System.out.println("u1 exists: " + ops.exists("users", "u1"));
        System.out.println("u99 exists: " + ops.exists("users", "u99"));

        // Partial update (only specified fields)
        ops.update("users", "u1", Map.of("age", 31, "status", "PREMIUM"));

        // Painless script update
        ops.updateScript("users", "u2", "ctx._source.age += params.delta", Map.of("delta", 1));

        // Upsert
        ops.upsert("users", "u99",
            new User("u99", "New User", "new@x.com", 20, "ACTIVE"),
            Map.of("age", 21)
        );

        // Delete
        boolean deleted = ops.delete("users", "u99");
        System.out.println("Deleted u99: " + deleted);

        // Force refresh so docs are searchable immediately
        idx.refresh("users");

        // =====================================================================
        // 4. Search — various query types
        // =====================================================================

        // match all
        EsResult<User> all = EsSearch.on(ops).index("users").matchAll().search(User.class);
        System.out.println("All users: " + all.total());

        // simple match (full-text)
        EsResult<User> byName = EsSearch.on(ops)
            .index("users")
            .match("name", "alice")
            .search(User.class);

        // multi-match
        EsResult<User> multiMatch = EsSearch.on(ops)
            .index("users")
            .multiMatch("alice", "name", "email")
            .search(User.class);

        // term (exact)
        EsResult<User> active = EsSearch.on(ops)
            .index("users")
            .term("status", "ACTIVE")
            .sort("age", true)
            .size(20)
            .search(User.class);
        System.out.println("Active users: " + active.total());

        // terms (IN list)
        EsResult<User> statusIn = EsSearch.on(ops)
            .index("users")
            .terms("status", "ACTIVE", "PREMIUM")
            .search(User.class);

        // range
        EsResult<User> young = EsSearch.on(ops)
            .index("users")
            .range("age", 20, 30)
            .sort("age", true)
            .search(User.class);

        // range with lambda (more control)
        EsResult<User> ageGte25 = EsSearch.on(ops)
            .index("users")
            .range("age", r -> r.gte(25).lt(35))
            .search(User.class);

        // bool query (must + filter + mustNot)
        EsResult<User> complex = EsSearch.on(ops)
            .index("users")
            .must(QueryBuilders.matchQuery("name", "alice"))
            .filter(QueryBuilders.termQuery("status", "ACTIVE"))
            .mustNot(QueryBuilders.termQuery("status", "INACTIVE"))
            .sort("age", false)
            .page(0, 10)
            .search(User.class);

        // ids query
        EsResult<User> byIds = EsSearch.on(ops)
            .index("users")
            .ids("u1", "u2", "u3")
            .search(User.class);

        // exists (field is present)
        EsResult<User> hasEmail = EsSearch.on(ops)
            .index("users")
            .exists("email")
            .search(User.class);

        // count only (no hits returned)
        long count = EsSearch.on(ops).index("users").term("status", "ACTIVE").count();
        System.out.println("Active count: " + count);

        // source filtering
        EsResult<User> partial2 = EsSearch.on(ops)
            .index("users")
            .matchAll()
            .includes("name", "email")   // only these fields
            .search(User.class);

        // =====================================================================
        // 5. Aggregations
        // =====================================================================

        EsResult<Map<String, Object>> aggResult = EsSearch.on(ops)
            .index("users")
            .matchAll()
            .aggTerms("by_status", "status", 10)
            .aggAvg("avg_age", "age")
            .aggMin("min_age", "age")
            .aggMax("max_age", "age")
            .aggSum("total_age", "age")
            .aggCardinality("unique_emails", "email")
            .size(0)            // no hits, only aggregations
            .searchRaw();

        EsAgg agg = EsAgg.from(aggResult);
        System.out.println("By status: " + agg.terms("by_status"));
        System.out.println("Avg age: " + agg.avg("avg_age"));
        System.out.println("Min age: " + agg.min("min_age"));
        System.out.println("Max age: " + agg.max("max_age"));
        System.out.println("Unique emails: " + agg.cardinality("unique_emails"));

        // Terms with sub-buckets
        agg.termsBuckets("by_status").forEach(b ->
            System.out.printf("  %s: %d%n", b.getKey(), b.getDocCount()));

        // =====================================================================
        // 6. Bulk operations
        // =====================================================================

        EsBulk.BulkResult bulkResult = EsBulk.on(ops)
            .index("users", "b1", new User("b1", "Bulk1", "b1@x.com", 20, "ACTIVE"))
            .index("users", "b2", new User("b2", "Bulk2", "b2@x.com", 21, "ACTIVE"))
            .update("users", "u1", Map.of("age", 32))
            .delete("users", "u3")
            .flush();

        System.out.println("Bulk: " + bulkResult);
        if (bulkResult.hasFailures()) {
            bulkResult.getFailures().forEach(f ->
                System.err.println("Failure: " + f.getFailureMessage()));
        }

        // Large import with auto-flush
        EsBulk bulk = EsBulk.on(ops).autoFlushAt(1000);
        for (int i = 100; i < 200; i++) {
            bulk.index("users", "gen" + i,
                new User("gen" + i, "User" + i, "u" + i + "@x.com", 20 + (i % 40), "ACTIVE"));
        }
        EsBulk.BulkResult finalResult = bulk.flush();
        System.out.println("Bulk import: " + finalResult);

        // =====================================================================
        // 7. Scroll (large exports)
        // =====================================================================

        idx.refresh("users");

        try (EsScroll<User> scroll = EsScroll.of(ops)
            .index("users")
            .matchAll()
            .sort("age", true)
            .batchSize(50)
            .keepAlive("2m")
            .open(User.class)) {

            int totalScrolled = 0;
            while (scroll.hasNext()) {
                List<User> page = scroll.next();
                totalScrolled += page.size();
                // process page...
            }
            System.out.println("Scrolled total: " + totalScrolled);
        }

        // forEach convenience
        try (EsScroll<User> scroll = EsScroll.of(ops)
            .index("users").term("status", "ACTIVE").open(User.class)) {
            scroll.forEach(u -> { /* process individual docs */ });
        }

        // =====================================================================
        // 8. Zero-downtime reindex
        // =====================================================================

        idx.create("users_v2", 2, 1,  ""
//                """
//                { "properties": {
//                    "name":   { "type": "text" },
//                    "email":  { "type": "keyword" },
//                    "age":    { "type": "integer" },
//                    "status": { "type": "keyword" },
//                    "score":  { "type": "float" }
//                }}
//            """
        );

        idx.addAlias("users", "users_alias");
        idx.reindex("users", "users_v2");
        idx.swapAlias("users", "users_v2", "users_alias");
        // idx.delete("users"); // optionally remove old

        // Or use the all-in-one helper:
        // idx.reindexWithAliasSwap("users_alias", "users_v2", "users_v3", 2, 1, newMapping, true);

        // =====================================================================
        // 9. Shutdown
        // =====================================================================
        factory.close();
    }
}
