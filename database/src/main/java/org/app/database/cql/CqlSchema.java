package org.app.database.cql;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.metadata.schema.KeyspaceMetadata;
import com.datastax.oss.driver.api.core.metadata.schema.TableMetadata;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Keyspace and table DDL management for Cassandra and ScyllaDB.
 * <p>
 * Use at application startup to ensure schema exists.
 * All methods are idempotent — safe to call on every startup with {@code IF NOT EXISTS}.
 *
 * <pre>
 * CqlSchema schema = CqlSchema.of(factory);
 *
 * // Create keyspace (idempotent)
 * schema.createKeyspaceSimple("myapp", 3);          // replication factor 3
 * schema.createKeyspaceNetworkTopology("myapp",
 *     Map.of("us-east", 3, "eu-west", 2));           // multi-DC
 *
 * // Create table (idempotent)
 * schema.createTable("""
 *     CREATE TABLE IF NOT EXISTS myapp.users (
 *         id        UUID,
 *         email     TEXT,
 *         name      TEXT,
 *         created   TIMESTAMP,
 *         PRIMARY KEY (id)
 *     )
 * """);
 *
 * // Create index
 * schema.createIndex("myapp", "users", "email");
 *
 * // Check existence
 * boolean exists = schema.tableExists("myapp", "users");
 *
 * // Describe
 * schema.describeTable("myapp", "users").ifPresent(System.out::println);
 * </pre>
 */
public final class CqlSchema {

    private final CqlSession session;

    private CqlSchema(CqlSession session) {
        this.session = session;
    }

    public static CqlSchema of(CqlSessionFactory factory) {
        return new CqlSchema(factory.session());
    }

    public static CqlSchema of(CqlSession session) {
        return new CqlSchema(session);
    }

    // -------------------------------------------------------------------------
    // Keyspace
    // -------------------------------------------------------------------------

    /**
     * Create keyspace with SimpleStrategy (single DC, dev/test).
     *
     * @param keyspace           keyspace name
     * @param replicationFactor  number of replicas
     */
    public void createKeyspaceSimple(String keyspace, int replicationFactor) {
        execute(String.format(
            "CREATE KEYSPACE IF NOT EXISTS %s" +
            " WITH replication = {'class': 'SimpleStrategy', 'replication_factor': %d}" +
            " AND durable_writes = true",
            keyspace, replicationFactor));
    }

    /**
     * Create keyspace with NetworkTopologyStrategy (production multi-DC).
     *
     * <pre>
     * schema.createKeyspaceNetworkTopology("myapp",
     *     Map.of("us-east", 3, "eu-west", 2));
     * </pre>
     */
    public void createKeyspaceNetworkTopology(String keyspace,
                                               Map<String, Integer> dcReplication) {
        StringBuilder dc = new StringBuilder();
        dcReplication.forEach((datacenter, rf) ->
            dc.append(String.format("'%s': %d, ", datacenter, rf)));
        String dcStr = dc.toString().replaceAll(", $", "");

        execute(String.format(
            "CREATE KEYSPACE IF NOT EXISTS %s" +
            " WITH replication = {'class': 'NetworkTopologyStrategy', %s}" +
            " AND durable_writes = true",
            keyspace, dcStr));
    }

    public boolean keyspaceExists(String keyspace) {
        return session.getMetadata().getKeyspace(keyspace).isPresent();
    }

    public void dropKeyspace(String keyspace) {
        execute("DROP KEYSPACE IF EXISTS " + keyspace);
    }

    // -------------------------------------------------------------------------
    // Table
    // -------------------------------------------------------------------------

    /**
     * Execute a CREATE TABLE IF NOT EXISTS statement.
     * Pass the full DDL string.
     *
     * <pre>
     * schema.createTable("""
     *     CREATE TABLE IF NOT EXISTS myapp.users (
     *         id       UUID,
     *         email    TEXT,
     *         name     TEXT,
     *         age      INT,
     *         tags     SET&lt;TEXT&gt;,
     *         meta     MAP&lt;TEXT, TEXT&gt;,
     *         created  TIMESTAMP,
     *         PRIMARY KEY (id)
     *     ) WITH default_time_to_live = 0
     *       AND gc_grace_seconds = 864000
     * """);
     * </pre>
     */
    public void createTable(String ddl) {
        execute(ddl);
    }

    /**
     * Shorthand — creates a table in the given keyspace from a partial DDL
     * (without the keyspace prefix). Adds {@code IF NOT EXISTS} automatically.
     */
    public void createTableIfAbsent(String keyspace, String tableName, String columnsDdl) {
        execute(String.format(
            "CREATE TABLE IF NOT EXISTS %s.%s (%s)",
            keyspace, tableName, columnsDdl));
    }

    public boolean tableExists(String keyspace, String table) {
        return session.getMetadata()
            .getKeyspace(keyspace)
            .flatMap(ks -> ks.getTable(table))
            .isPresent();
    }

    public void dropTable(String keyspace, String table) {
        execute(String.format("DROP TABLE IF EXISTS %s.%s", keyspace, table));
    }

    public void truncateTable(String keyspace, String table) {
        execute(String.format("TRUNCATE %s.%s", keyspace, table));
    }

    /**
     * ALTER TABLE — add a column.
     */
    public void addColumn(String keyspace, String table, String column, String type) {
        execute(String.format(
            "ALTER TABLE %s.%s ADD %s %s", keyspace, table, column, type));
    }

    /**
     * ALTER TABLE — drop a column.
     */
    public void dropColumn(String keyspace, String table, String column) {
        execute(String.format(
            "ALTER TABLE %s.%s DROP %s", keyspace, table, column));
    }

    // -------------------------------------------------------------------------
    // Index
    // -------------------------------------------------------------------------

    /**
     * Create a secondary index on a column (SAI for ScyllaDB, legacy for Cassandra).
     */
    public void createIndex(String keyspace, String table, String column) {
        String indexName = table + "_" + column + "_idx";
        execute(String.format(
            "CREATE INDEX IF NOT EXISTS %s ON %s.%s (%s)",
            indexName, keyspace, table, column));
    }

    /**
     * Create a ScyllaDB Storage-Attached Index (SAI) — faster than Cassandra 2i.
     * Requires ScyllaDB 5.x+ or Cassandra 4.1+.
     */
    public void createSAI(String keyspace, String table, String column) {
        String indexName = table + "_" + column + "_sai";
        execute(String.format(
            "CREATE INDEX IF NOT EXISTS %s ON %s.%s (%s) USING 'sai'",
            indexName, keyspace, table, column));
    }

    public void dropIndex(String keyspace, String indexName) {
        execute(String.format("DROP INDEX IF EXISTS %s.%s", keyspace, indexName));
    }

    // -------------------------------------------------------------------------
    // Materialized Views
    // -------------------------------------------------------------------------

    /**
     * Create a materialized view.
     *
     * <pre>
     * schema.createMaterializedView("""
     *     CREATE MATERIALIZED VIEW IF NOT EXISTS myapp.users_by_email AS
     *         SELECT id, email, name FROM myapp.users
     *         WHERE email IS NOT NULL AND id IS NOT NULL
     *         PRIMARY KEY (email, id)
     *     WITH CLUSTERING ORDER BY (id ASC)
     * """);
     * </pre>
     */
    public void createMaterializedView(String ddl) {
        execute(ddl);
    }

    public void dropMaterializedView(String keyspace, String viewName) {
        execute(String.format("DROP MATERIALIZED VIEW IF EXISTS %s.%s", keyspace, viewName));
    }

    // -------------------------------------------------------------------------
    // User-Defined Types (UDT)
    // -------------------------------------------------------------------------

    /**
     * Create a UDT — used for nested structured data in columns.
     *
     * <pre>
     * schema.createType("myapp", "address",
     *     "street TEXT, city TEXT, zip TEXT, country TEXT");
     * </pre>
     */
    public void createType(String keyspace, String typeName, String fieldsDdl) {
        execute(String.format(
            "CREATE TYPE IF NOT EXISTS %s.%s (%s)", keyspace, typeName, fieldsDdl));
    }

    public void dropType(String keyspace, String typeName) {
        execute(String.format("DROP TYPE IF EXISTS %s.%s", keyspace, typeName));
    }

    // -------------------------------------------------------------------------
    // Describe / Introspect
    // -------------------------------------------------------------------------

    public Optional<String> describeTable(String keyspace, String table) {
        return session.getMetadata()
            .getKeyspace(keyspace)
            .flatMap(ks -> ks.getTable(table))
            .map(TableMetadata::describe);
    }

    public Optional<String> describeKeyspace(String keyspace) {
        return session.getMetadata()
            .getKeyspace(keyspace)
            .map(KeyspaceMetadata::describe);
    }

    public Set<String> listTables(String keyspace) {
        return session.getMetadata()
            .getKeyspace(keyspace)
            .map(ks -> ks.getTables().keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());
    }

    // -------------------------------------------------------------------------
    // Raw DDL execute
    // -------------------------------------------------------------------------

    /** Execute any DDL statement. */
    public void execute(String ddl) {
        session.execute(SimpleStatement.newInstance(ddl.trim()));
    }

    // -------------------------------------------------------------------------
    // Schema migration helpers
    // -------------------------------------------------------------------------

    /**
     * Run a list of DDL statements in order (simple sequential migration).
     * Each statement is executed independently — no transaction.
     * All statements should use {@code IF NOT EXISTS} / {@code IF EXISTS}
     * for idempotency.
     */
    public void migrate(List<String> ddlStatements) {
        for (String ddl : ddlStatements) {
            if (!ddl.isBlank()) execute(ddl);
        }
    }
}
