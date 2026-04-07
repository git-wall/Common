package org.app.database.cql;

import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.Row;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Complete usage demo for the Cassandra / ScyllaDB utility module.
 * Java 11 compatible — no records, no text blocks, no switch expressions.
 *
 * Run with:
 *   Cassandra: docker run -d --name cass -p 9042:9042 cassandra:4.1
 *   ScyllaDB:  docker run -d --name scylla -p 9042:9042 scylladb/scylla --smp 1
 */
public class CqlUsageExample {

    // ── POJO — annotation-based mapping ──────────────────────────────────────

    static class User {
        @CqlMapper.CqlColumn("id")      public UUID    id;
        @CqlMapper.CqlColumn("email")   public String  email;
        @CqlMapper.CqlColumn("name")    public String  name;
        @CqlMapper.CqlColumn("age")     public int     age;
        @CqlMapper.CqlColumn("created") public Instant created;

        User() {}

        @Override
        public String toString() {
            return String.format("User{id=%s, name='%s', email='%s', age=%d}",
                id, name, email, age);
        }
    }

    // ── Order — plain class (replaces record, which needs Java 16+) ───────────

    static class Order {
        private final UUID    id;
        private final UUID    userId;
        private final double  amount;
        private final String  status;
        private final Instant createdAt;

        Order(UUID id, UUID userId, double amount, String status, Instant createdAt) {
            this.id        = id;
            this.userId    = userId;
            this.amount    = amount;
            this.status    = status;
            this.createdAt = createdAt;
        }

        static Order from(Row row) {
            return new Order(
                row.getUuid("id"),
                row.getUuid("user_id"),
                row.getDouble("amount"),
                row.getString("status"),
                row.getInstant("created_at")
            );
        }

        @Override
        public String toString() {
            return String.format("Order{id=%s, amount=%.2f, status='%s'}", id, amount, status);
        }
    }

    public static void main(String[] args) throws Exception {

        // =====================================================================
        // 1. Setup
        // =====================================================================

        CqlConfig config = CqlConfig.builder()
            .contactPoints(Arrays.asList("127.0.0.1:9042"))
            .localDatacenter("datacenter1")   // check: SELECT data_center FROM system.local
            .keyspace("demo")
            .consistencyLevel("LOCAL_QUORUM")
            .requestTimeout(Duration.ofSeconds(10))
            .build();

        // Same driver config works for ScyllaDB — just change contactPoints + localDatacenter.
        // ScyllaDB automatically enables shard-aware routing when the driver detects its nodes.

        CqlSessionFactory factory = CqlSessionFactory.of(config);
        System.out.println("Connected to DC: " + factory.ping());

        CqlOps      ops    = CqlOps.of(factory);
        CqlAsync    async  = CqlAsync.of(factory);
        CqlSchema   schema = CqlSchema.of(factory);
        CqlPaging   paging = CqlPaging.of(factory);
        CqlPrepared prep   = CqlPrepared.of(factory);

        // =====================================================================
        // 2. Schema — idempotent, safe to call on every startup
        // =====================================================================

        schema.createKeyspaceSimple("demo", 1);

        // Production multi-DC example:
        // Map<String, Integer> dc = new HashMap<>();
        // dc.put("us-east", 3);
        // dc.put("eu-west", 2);
        // schema.createKeyspaceNetworkTopology("demo", dc);

        // String concat avoids text blocks (Java 15+)
        schema.createTable(
            "CREATE TABLE IF NOT EXISTS demo.users ("
            + "  id        UUID,"
            + "  email     TEXT,"
            + "  name      TEXT,"
            + "  age       INT,"
            + "  created   TIMESTAMP,"
            + "  tags      SET<TEXT>,"
            + "  meta      MAP<TEXT, TEXT>,"
            + "  PRIMARY KEY (id)"
            + ") WITH gc_grace_seconds = 864000"
        );

        schema.createTable(
            "CREATE TABLE IF NOT EXISTS demo.users_by_email ("
            + "  email   TEXT,"
            + "  id      UUID,"
            + "  name    TEXT,"
            + "  PRIMARY KEY (email)"
            + ")"
        );

        schema.createTable(
            "CREATE TABLE IF NOT EXISTS demo.orders ("
            + "  user_id     UUID,"
            + "  id          UUID,"
            + "  amount      DOUBLE,"
            + "  status      TEXT,"
            + "  created_at  TIMESTAMP,"
            + "  PRIMARY KEY (user_id, id)"
            + ") WITH CLUSTERING ORDER BY (id DESC)"
            + "  AND default_time_to_live = 7776000"
        );

        System.out.println("Tables: " + schema.listTables("demo"));

        // =====================================================================
        // 3. Insert
        // =====================================================================

        UUID userId = UUID.randomUUID();
        ops.write(
            "INSERT INTO demo.users (id, email, name, age, created) VALUES (?, ?, ?, ?, ?)",
            userId, "alice@x.com", "Alice", 30, Instant.now());

        ops.write(
            "INSERT INTO demo.users_by_email (email, id, name) VALUES (?, ?, ?)",
            "alice@x.com", userId, "Alice");

        UUID userId2 = UUID.randomUUID();
        CqlBatch.logged(factory)
            .add("INSERT INTO demo.users (id, email, name, age, created) VALUES (?, ?, ?, ?, ?)",
                 userId2, "bob@x.com", "Bob", 25, Instant.now())
            .add("INSERT INTO demo.users_by_email (email, id, name) VALUES (?, ?, ?)",
                 "bob@x.com", userId2, "Bob")
            .execute();
        System.out.println("Users inserted");

        // =====================================================================
        // 4. Select — sync
        // =====================================================================

        // Annotation-based mapping
        Optional<User> u = ops.selectOne(
            "SELECT * FROM demo.users WHERE id = ?", User.class, userId);
        u.ifPresent(user -> System.out.println("Found: " + user));

        // Lambda mapping
        Optional<User> u2 = ops.selectOne(
            "SELECT * FROM demo.users WHERE id = ?",
            row -> {
                User user = new User();
                user.id    = row.getUuid("id");
                user.name  = row.getString("name");
                user.email = row.getString("email");
                user.age   = row.getInt("age");
                return user;
            },
            userId2);
        u2.ifPresent(user -> System.out.println("Lambda mapped: " + user));

        // Denormalized lookup by email
        Optional<Row> byEmail = ops.selectRow(
            "SELECT * FROM demo.users_by_email WHERE email = ?", "alice@x.com");
        byEmail.ifPresent(row -> System.out.println("By email: " + row.getString("name")));

        // =====================================================================
        // 5. Orders — clustering key range query
        // =====================================================================

        for (int i = 0; i < 5; i++) {
            ops.write(
                "INSERT INTO demo.orders (user_id, id, amount, status, created_at) VALUES (?, ?, ?, ?, ?)",
                userId, UUID.randomUUID(), 10.0 + i * 5, "PENDING", Instant.now());
        }

        List<Order> orders = ops.selectList(
            "SELECT * FROM demo.orders WHERE user_id = ? LIMIT 10",
            Order::from, userId);
        System.out.println("Orders: " + orders.size());

        long orderCount = ops.count(
            "SELECT COUNT(*) FROM demo.orders WHERE user_id = ?", userId);
        System.out.println("Order count: " + orderCount);

        // =====================================================================
        // 6. Update
        // =====================================================================

        ops.write("UPDATE demo.users SET age = ? WHERE id = ?", 31, userId);

        ops.write("UPDATE demo.users SET tags = tags + ? WHERE id = ?",
            new HashSet<>(Arrays.asList("premium", "verified")), userId);

        Map<String, String> metaUpdate = new HashMap<>();
        metaUpdate.put("tier",   "gold");
        metaUpdate.put("region", "us-east");
        ops.write("UPDATE demo.users SET meta = meta + ? WHERE id = ?", metaUpdate, userId);

        // =====================================================================
        // 7. LWT — Lightweight Transactions
        // =====================================================================

        boolean inserted = ops.insertIfNotExists(
            "INSERT INTO demo.users_by_email (email, id, name) VALUES (?, ?, ?) IF NOT EXISTS",
            "charlie@x.com", UUID.randomUUID(), "Charlie");
        System.out.println("Registered charlie: " + inserted);

        boolean duplicate = ops.insertIfNotExists(
            "INSERT INTO demo.users_by_email (email, id, name) VALUES (?, ?, ?) IF NOT EXISTS",
            "charlie@x.com", UUID.randomUUID(), "Charlie2");
        System.out.println("Duplicate rejected: " + !duplicate);

        // Optimistic CAS update
        CqlOps.LwtResult result = ops.executeIf(
            "UPDATE demo.users SET age = ? WHERE id = ? IF age = ?",
            32, userId, 31);
        System.out.println("CAS update applied: " + result.applied());
        if (!result.applied()) {
            System.out.println("Conflict — existing age: "
                + result.existing("age", Integer.class));
        }

        // =====================================================================
        // 8. Delete
        // =====================================================================

        ops.write("DELETE tags FROM demo.users WHERE id = ?", userId);
        ops.write("DELETE FROM demo.users WHERE id = ?", UUID.randomUUID());

        boolean deleted = ops.deleteIf(
            "DELETE FROM demo.users_by_email WHERE email = ? IF EXISTS",
            "charlie@x.com");
        System.out.println("Deleted charlie: " + deleted);

        // =====================================================================
        // 9. Batch
        // =====================================================================

        CqlBatch unloggedBatch = CqlBatch.unlogged(factory);
        for (int i = 0; i < 10; i++) {
            unloggedBatch.add(
                "INSERT INTO demo.orders (user_id, id, amount, status, created_at) VALUES (?, ?, ?, ?, ?)",
                userId, UUID.randomUUID(), 99.0 + i, "NEW", Instant.now());
        }
        unloggedBatch.execute();
        System.out.println("Unlogged batch done");

        CqlBatch.logged(factory)
            .add("UPDATE demo.users SET name = ? WHERE id = ?", "Alice Updated", userId)
            .add("INSERT INTO demo.users_by_email (email, id, name) VALUES (?, ?, ?)",
                 "alice_new@x.com", userId, "Alice Updated")
            .executeAsync()
            .thenRun(() -> System.out.println("Async batch done"));

        // =====================================================================
        // 10. Async operations
        // =====================================================================

        CompletableFuture<Optional<User>> af = async.selectOne(
            "SELECT * FROM demo.users WHERE id = ?", User.class, userId);
        af.thenAccept(ou -> ou.ifPresent(user -> System.out.println("Async found: " + user)));

        CompletableFuture<Void> aw = async.write(
            "UPDATE demo.users SET age = ? WHERE id = ?", 33, userId);
        aw.thenRun(() -> System.out.println("Async write done"));

        // Fire 20 writes in parallel — collect(Collectors.toList()) not .toList()
        List<CompletableFuture<Void>> parallelWrites = IntStream.range(0, 20)
            .mapToObj(i -> async.write(
                "INSERT INTO demo.orders (user_id, id, amount, status, created_at) VALUES (?, ?, ?, ?, ?)",
                userId, UUID.randomUUID(), i * 1.5, "ASYNC", Instant.now()))
            .collect(Collectors.toList());
        CompletableFuture.allOf(parallelWrites.toArray(new CompletableFuture[0])).join();
        System.out.println("20 parallel async writes done");

        async.insertIfNotExists(
            "INSERT INTO demo.users_by_email (email, id, name) VALUES (?, ?, ?) IF NOT EXISTS",
            "async@x.com", UUID.randomUUID(), "Async User")
            .thenAccept(ok -> System.out.println("Async LWT: " + ok));

        // =====================================================================
        // 11. Paging
        // =====================================================================

        paging.forEach(
            "SELECT * FROM demo.orders WHERE user_id = ?",
            Order::from, 200,
            batch -> System.out.println("Page of " + batch.size() + " orders"),
            userId);

        CqlPaging.Page<Order> page1 = paging.page(
            "SELECT * FROM demo.orders WHERE user_id = ?",
            Order::from, 5, null, userId);
        System.out.println("Page1 size=" + page1.size() + " hasNext=" + page1.hasNext());

        if (page1.hasNext()) {
            CqlPaging.Page<Order> page2 = paging.page(
                "SELECT * FROM demo.orders WHERE user_id = ?",
                Order::from, 5, page1.nextCursor(), userId);
            System.out.println("Page2 size=" + page2.size() + " isLast=" + page2.isLast());
        }

        paging.forEachAsync(
            "SELECT * FROM demo.orders WHERE user_id = ?",
            Order::from, 100,
            batch -> System.out.println("Async page: " + batch.size()),
            userId).join();

        // =====================================================================
        // 12. Prepared statement cache
        // =====================================================================

        PreparedStatement ps = prep.get("SELECT * FROM demo.users WHERE id = ?");
        for (UUID id : Arrays.asList(userId, userId2)) {
            BoundStatement bs = ps.bind(id);
            ops.execute(bs).one();
        }
        System.out.println("Prepared cache size: " + prep.size());

        // =====================================================================
        // 13. CqlMapper — manual lambda registration
        // =====================================================================

        CqlMapper mapper = new CqlMapper();
        mapper.register(Order.class, Order::from);

        List<Order> mappedOrders = mapper.mapRows(
            ops.execute("SELECT * FROM demo.orders WHERE user_id = ? LIMIT 5", userId),
            Order.class);
        System.out.println("Mapped orders: " + mappedOrders.size());

        List<Order> mappedOrders1 = mapper.mapRows(
            ops.execute("SELECT * FROM demo.orders WHERE user_id = ? LIMIT 5", userId),
            Order::from);
        System.out.println("Mapped orders: " + mappedOrders1.size());

        // =====================================================================
        // 14. Schema inspection
        // =====================================================================

        System.out.println("Table exists: " + schema.tableExists("demo", "users"));
        schema.describeTable("demo", "users").ifPresent(d ->
            System.out.println("Schema: " + d.substring(0, Math.min(80, d.length())) + "..."));

        // =====================================================================
        // 15. Shutdown
        // =====================================================================

        factory.close();
        System.out.println("Done.");
    }
}
