package org.app.database.mybatis;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.mybatis.dynamic.sql.SqlColumn;
import org.mybatis.dynamic.sql.SqlTable;
import org.mybatis.dynamic.sql.insert.render.GeneralInsertStatementProvider;
import org.mybatis.dynamic.sql.render.RenderingStrategies;
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider;
import org.mybatis.dynamic.sql.update.render.UpdateStatementProvider;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.mybatis.dynamic.sql.SqlBuilder.*;

/**
 * Complete usage example — Java 11, no Spring, zero reflection in hot paths.
 *
 * <h3>Insert API — which path to use</h3>
 * <pre>
 *  Insert 1 row, need generated key?
 *    └─ insertInto(table).set(col).toValue(val) → GeneralInsertStatementProvider
 *       db.insert(stmt)
 *       SQL:  INSERT INTO t (a, b) VALUES (?, ?)
 *       Note: getParameters() ✅ — JDBC-bindable, zero reflection.
 *
 *  Insert N rows, fastest (1 round-trip)?
 *    └─ db.insertMultiValues(table, columns, List&lt;Object[]&gt;)
 *       SQL:  INSERT INTO t (a, b) VALUES (?,?),(?,?),(?,?)
 *       Note: DynSql builds SQL internally. Zero reflection.
 *
 *  Insert N rows, very large volume (multi-values SQL too big)?
 *    └─ db.rawBatch(sql, List&lt;Object[]&gt;)
 *       Uses JDBC addBatch/executeBatch. Multiple round-trips. Zero reflection.
 *
 *  ✗ MultiRowInsertStatementProvider — NOT supported with plain JDBC.
 *    insertMultiple().into().map().toProperty() renders #{records[0].name}
 *    (MyBatis ORM syntax). Has NO getParameters(). Not bindable with PreparedStatement.
 *
 *  ✗ InsertStatementProvider (record-based) — NOT supported with plain JDBC.
 *    insert(record).into().map().toProperty() renders #{row.name}
 *    (MyBatis ORM syntax). Has NO getParameters(). Not bindable with PreparedStatement.
 * </pre>
 */
public class DynSqlExample {

    // =========================================================================
    // 1. TABLE + COLUMN DEFINITIONS
    //    One static class per table. SqlColumn<T> gives compile-time type safety
    //    in WHERE clauses. Define once, reuse everywhere — no annotation processing.
    // =========================================================================

    static final class UserTable extends SqlTable {

        static final UserTable TABLE = new UserTable();

        final SqlColumn<Long>          id        = column("id");
        final SqlColumn<String>        name      = column("name");
        final SqlColumn<String>        email     = column("email");
        final SqlColumn<String>        status    = column("status");
        final SqlColumn<BigDecimal>    balance   = column("balance");
        final SqlColumn<LocalDateTime> createdAt = column("created_at");
        final SqlColumn<LocalDateTime> updatedAt = column("updated_at");

        private UserTable() { super("users"); }
    }

    static final class OrderTable extends SqlTable {

        static final OrderTable TABLE = new OrderTable();

        final SqlColumn<Long>          id        = column("id");
        final SqlColumn<Long>          userId    = column("user_id");
        final SqlColumn<String>        product   = column("product");
        final SqlColumn<BigDecimal>    amount    = column("amount");
        final SqlColumn<String>        status    = column("status");
        final SqlColumn<LocalDateTime> createdAt = column("created_at");

        private OrderTable() { super("orders"); }
    }

    // =========================================================================
    // 2. DOMAIN CLASSES
    // =========================================================================

    static final class User {
        final long          id;
        final String        name;
        final String        email;
        final String        status;
        final BigDecimal    balance;
        final LocalDateTime createdAt;

        User(long id, String name, String email, String status,
             BigDecimal balance, LocalDateTime createdAt) {
            this.id        = id;
            this.name      = name;
            this.email     = email;
            this.status    = status;
            this.balance   = balance;
            this.createdAt = createdAt;
        }

        @Override public String toString() {
            return String.format("User{id=%d, name='%s', status='%s', balance=%s}",
                id, name, status, balance);
        }
    }

    static final class Order {
        final long          id;
        final long          userId;
        final String        product;
        final BigDecimal    amount;
        final String        status;
        final LocalDateTime createdAt;

        Order(long id, long userId, String product, BigDecimal amount,
              String status, LocalDateTime createdAt) {
            this.id        = id;
            this.userId    = userId;
            this.product   = product;
            this.amount    = amount;
            this.status    = status;
            this.createdAt = createdAt;
        }

        @Override public String toString() {
            return String.format("Order{id=%d, product='%s', amount=%s}", id, product, amount);
        }
    }

    // =========================================================================
    // 3. ROW MAPPERS — static constants, define once, reuse everywhere.
    //    Direct rs.getXxx() calls — zero reflection, fastest possible mapping.
    // =========================================================================

    static final RowMapper<User> USER_MAPPER = rs -> new User(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("email"),
        rs.getString("status"),
        rs.getBigDecimal("balance"),
        rs.getTimestamp("created_at") != null
            ? rs.getTimestamp("created_at").toLocalDateTime() : null
    );

    static final RowMapper<Order> ORDER_MAPPER = rs -> new Order(
        rs.getLong("id"),
        rs.getLong("user_id"),
        rs.getString("product"),
        rs.getBigDecimal("amount"),
        rs.getString("status"),
        rs.getTimestamp("created_at") != null
            ? rs.getTimestamp("created_at").toLocalDateTime() : null
    );

    // =========================================================================
    // 4. DATASOURCE SETUP — caller provides this, module doesn't care which pool
    // =========================================================================

    static DataSource createDataSource() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:h2:mem:demo;DB_CLOSE_DELAY=-1");
        cfg.setDriverClassName("org.h2.Driver");
        cfg.setMaximumPoolSize(10);
        cfg.setMinimumIdle(2);
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        return new HikariDataSource(cfg);
    }

    static void createSchema(DynSql db) {
        db.rawExecute(
            "CREATE TABLE IF NOT EXISTS users ("
                + "  id         BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "  name       VARCHAR(100) NOT NULL,"
                + "  email      VARCHAR(200) NOT NULL UNIQUE,"
                + "  status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',"
                + "  balance    DECIMAL(12,2) NOT NULL DEFAULT 0,"
                + "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                + "  updated_at TIMESTAMP NULL"
                + ")"
        );
        db.rawExecute(
            "CREATE TABLE IF NOT EXISTS orders ("
                + "  id         BIGINT AUTO_INCREMENT PRIMARY KEY,"
                + "  user_id    BIGINT NOT NULL,"
                + "  product    VARCHAR(200) NOT NULL,"
                + "  amount     DECIMAL(12,2) NOT NULL,"
                + "  status     VARCHAR(20) NOT NULL DEFAULT 'PENDING',"
                + "  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                + ")"
        );
    }

    // =========================================================================
    // 5. REPOSITORY — optional pattern wrapping all queries for one entity
    // =========================================================================

    static final class UserRepository {

        private final DynSql    db;
        private final UserTable t = UserTable.TABLE;

        UserRepository(DynSql db) { this.db = db; }

        // ── INSERT PATH A: single row ─────────────────────────────────────────
        // insertInto(table).set(col).toValue(val) → GeneralInsertStatementProvider
        // Has: getInsertStatement() + getParameters() → JDBC-bindable, zero reflection.
        // SQL: INSERT INTO users (name, email, status, balance, created_at) VALUES (?,?,?,?,?)

        public Long insert(String name, String email) {
            GeneralInsertStatementProvider stmt = insertInto(t)
                .set(t.name).toValue(name)
                .set(t.email).toValue(email)
                .set(t.status).toValue("ACTIVE")
                .set(t.balance).toValue(BigDecimal.ZERO)
                .set(t.createdAt).toValue(LocalDateTime.now())
                .build().render(RenderingStrategies.MYBATIS3);

            return db.insert(stmt).generatedKey(Long.class);
        }

        // ── INSERT PATH B: multi-values, 1 round-trip (fastest bulk) ─────────
        // db.insertMultiValues builds:
        // INSERT INTO users (name, email, status, balance, created_at)
        // VALUES (?,?,?,?,?),(?,?,?,?,?),(?,?,?,?,?)
        // Zero reflection — caller builds Object[] rows directly.

        public SqlResult insertBulk(List<User> users) {
            List<Object[]> rows = users.stream()
                .map(u -> new Object[]{
                    u.name, u.email, "ACTIVE", BigDecimal.ZERO, LocalDateTime.now()
                })
                .collect(Collectors.toList());

            return db.insertMultiValues(
                "users",
                Arrays.asList("name", "email", "status", "balance", "created_at"),
                rows
            );
        }

        // ── INSERT PATH C: rawBatch — addBatch/executeBatch ───────────────────
        // Multiple round-trips. Use when volume is very large (thousands of rows)
        // and a single multi-values SQL would be too long for the DB.
        // Also zero reflection.

        public SqlResult insertBulkLarge(List<User> users) {
            List<Object[]> rows = users.stream()
                .map(u -> new Object[]{ u.name, u.email })
                .collect(Collectors.toList());

            return db.rawBatch(
                "INSERT INTO users (name, email, status, balance, created_at)"
                    + " VALUES (?, ?, 'ACTIVE', 0, NOW())",
                rows
            );
        }

        // ── SELECT ────────────────────────────────────────────────────────────

        public Optional<User> findById(long id) {
            SelectStatementProvider stmt = select(t.id, t.name, t.email,
                t.status, t.balance, t.createdAt)
                .from(t)
                .where(t.id, isEqualTo(id))
                .build().render(RenderingStrategies.MYBATIS3);
            return db.selectOne(stmt, USER_MAPPER);
        }

        public Optional<User> findByEmail(String email) {
            SelectStatementProvider stmt = select(t.id, t.name, t.email,
                t.status, t.balance, t.createdAt)
                .from(t)
                .where(t.email, isEqualTo(email))
                .build().render(RenderingStrategies.MYBATIS3);
            return db.selectOne(stmt, USER_MAPPER);
        }

        public List<User> findByStatus(String status) {
            SelectStatementProvider stmt = select(t.id, t.name, t.email,
                t.status, t.balance, t.createdAt)
                .from(t)
                .where(t.status, isEqualTo(status))
                .orderBy(t.name)
                .build().render(RenderingStrategies.MYBATIS3);
            return db.selectList(stmt, USER_MAPPER);
        }

        public List<User> findByIds(List<Long> ids) {
            SelectStatementProvider stmt = select(t.id, t.name, t.email,
                t.status, t.balance, t.createdAt)
                .from(t)
                .where(t.id, isIn(ids))
                .orderBy(t.id)
                .build().render(RenderingStrategies.MYBATIS3);
            return db.selectList(stmt, USER_MAPPER);
        }

        public List<User> findByNameLike(String pattern) {
            SelectStatementProvider stmt = select(t.id, t.name, t.email,
                t.status, t.balance, t.createdAt)
                .from(t)
                .where(t.name, isLike("%" + pattern + "%"))
                .orderBy(t.name)
                .build().render(RenderingStrategies.MYBATIS3);
            return db.selectList(stmt, USER_MAPPER);
        }

        public PageResult<User> findPage(String status, int page, int pageSize) {
            int offset = (page - 1) * pageSize;

            SelectStatementProvider dataStmt = select(t.id, t.name, t.email,
                t.status, t.balance, t.createdAt)
                .from(t)
                .where(t.status, isEqualTo(status))
                .orderBy(t.createdAt.descending())
                .limit(pageSize).offset(offset)
                .build().render(RenderingStrategies.MYBATIS3);

            SelectStatementProvider countStmt = select(count())
                .from(t)
                .where(t.status, isEqualTo(status))
                .build().render(RenderingStrategies.MYBATIS3);

            return db.selectPage(dataStmt, countStmt, USER_MAPPER, page, pageSize);
        }

        public long countByStatus(String status) {
            return db.selectCount(
                select(count()).from(t)
                    .where(t.status, isEqualTo(status))
                    .build().render(RenderingStrategies.MYBATIS3)
            );
        }

        public boolean emailExists(String email) {
            return db.exists(
                select(constant("1")).from(t)
                    .where(t.email, isEqualTo(email))
                    .limit(1)
                    .build().render(RenderingStrategies.MYBATIS3)
            );
        }

        // ── UPDATE ────────────────────────────────────────────────────────────

        public int updateStatus(long id, String status) {
            UpdateStatementProvider stmt = update(t)
                .set(t.status).equalTo(status)
                .set(t.updatedAt).equalTo(LocalDateTime.now())
                .where(t.id, isEqualTo(id))
                .build().render(RenderingStrategies.MYBATIS3);
            return db.update(stmt).rowsAffected();
        }

        public int addBalance(long id, BigDecimal delta) {
            // Arithmetic on column value → raw SQL
            return db.rawExecute(
                "UPDATE users SET balance = balance + ?, updated_at = NOW() WHERE id = ?",
                delta, id
            ).rowsAffected();
        }

        // ── DELETE ────────────────────────────────────────────────────────────

        public int delete(long id) {
            return db.delete(
                deleteFrom(t).where(t.id, isEqualTo(id))
                    .build().render(RenderingStrategies.MYBATIS3)
            ).rowsAffected();
        }

        // ── STREAM ────────────────────────────────────────────────────────────

        public void streamAll(java.util.function.Consumer<User> consumer) {
            db.selectStream(
                select(t.id, t.name, t.email, t.status, t.balance, t.createdAt)
                    .from(t).orderBy(t.id)
                    .build().render(RenderingStrategies.MYBATIS3),
                USER_MAPPER, consumer
            );
        }
    }

    // =========================================================================
    // 6. MAIN
    // =========================================================================

    public static void main(String[] args) {

        DataSource ds = createDataSource();
        DynSql     db = DynSql.of(ds);
        createSchema(db);
        System.out.println("=== Schema ready ===\n");

        UserRepository users = new UserRepository(db);
        UserTable      ut    = UserTable.TABLE;
        OrderTable     ot    = OrderTable.TABLE;

        // ── INSERT PATH A — single row, GeneralInsertStatementProvider ─────────
        // SQL: INSERT INTO users (name,email,status,balance,created_at) VALUES (?,?,?,?,?)
        // getParameters() ✅ — LinkedHashMap, insertion-ordered, JDBC-bindable.

        Long aliceId = users.insert("Alice", "alice@example.com");
        Long bobId   = users.insert("Bob",   "bob@example.com");
        System.out.println("[A] insert: alice=" + aliceId + " bob=" + bobId);

        // ── INSERT PATH B — insertMultiValues, 1 round-trip ───────────────────
        // SQL: INSERT INTO users (name,email,status,balance,created_at)
        //      VALUES (?,?,?,?,?),(?,?,?,?,?),(?,?,?,?,?)
        // DynSql builds the SQL dynamically from column list + rows.
        // Zero reflection. One PreparedStatement. One network round-trip.

        List<User> batch = Arrays.asList(
            new User(0, "Carol", "carol@example.com", "ACTIVE", BigDecimal.ZERO, null),
            new User(0, "Dave",  "dave@example.com",  "ACTIVE", BigDecimal.ZERO, null),
            new User(0, "Eve",   "eve@example.com",   "ACTIVE", BigDecimal.ZERO, null)
        );
        SqlResult bulkR = users.insertBulk(batch);
        System.out.println("[B] insertMultiValues: " + bulkR.rowsAffected() + " rows, 1 round-trip");

        // ── INSERT PATH C — rawBatch, addBatch/executeBatch ───────────────────
        // Multiple round-trips. Use for very large sets where multi-values SQL is too long.

        List<User> more = Arrays.asList(
            new User(0, "Frank", "frank@example.com", "ACTIVE", BigDecimal.ZERO, null),
            new User(0, "Grace", "grace@example.com", "ACTIVE", BigDecimal.ZERO, null)
        );
        SqlResult batchR = users.insertBulkLarge(more);
        System.out.println("[C] rawBatch: " + batchR.rowsAffected() + " rows");

        // ── SELECT ────────────────────────────────────────────────────────────

        users.findById(aliceId).ifPresent(u -> System.out.println("\nfindById: " + u));
        System.out.println("findById ghost: " + users.findById(99999L).isPresent());

        List<User> active = users.findByStatus("ACTIVE");
        System.out.println("active: " + active.size());

        List<User> byIds = users.findByIds(Arrays.asList(aliceId, bobId));
        System.out.println("findByIds: " + byIds.size());

        List<User> byName = users.findByNameLike("a");
        System.out.println("LIKE '%a%': " + byName.size());

        // ── PAGING ────────────────────────────────────────────────────────────

        PageResult<User> page1 = users.findPage("ACTIVE", 1, 3);
        System.out.println("\npage1: " + page1
            + " hasNext=" + page1.hasNext()
            + " totalPages=" + page1.totalPages());

        if (page1.hasNext()) {
            PageResult<User> page2 = users.findPage("ACTIVE", 2, 3);
            System.out.println("page2: " + page2);
        }

        // ── COUNT / EXISTS ────────────────────────────────────────────────────

        System.out.println("\ncountActive: " + users.countByStatus("ACTIVE"));
        System.out.println("emailExists alice: " + users.emailExists("alice@example.com"));
        System.out.println("emailExists ghost: " + users.emailExists("nobody@x.com"));

        // ── SCALAR ────────────────────────────────────────────────────────────

        db.rawExecute("UPDATE users SET balance = 100 WHERE id = ?", aliceId);
        db.rawExecute("UPDATE users SET balance = 200 WHERE id = ?", bobId);

        Optional<BigDecimal> total = db.selectScalar(
            select(sum(ut.balance)).from(ut)
                .where(ut.status, isEqualTo("ACTIVE"))
                .build().render(RenderingStrategies.MYBATIS3),
            rs -> rs.getBigDecimal(1)
        );
        System.out.println("total balance: " + total.orElse(BigDecimal.ZERO));

        // ── UPDATE ────────────────────────────────────────────────────────────

        users.updateStatus(bobId, "INACTIVE");
        users.findById(bobId).ifPresent(u -> System.out.println("\nbob: " + u));

        users.addBalance(aliceId, new BigDecimal("50"));
        users.findById(aliceId).ifPresent(u -> System.out.println("alice: " + u));

        // ── UPDATE BATCH ──────────────────────────────────────────────────────

        List<UpdateStatementProvider> updateStmts = Arrays.asList(aliceId, bobId)
            .stream()
            .map(id -> update(ut)
                .set(ut.updatedAt).equalTo(LocalDateTime.now())
                .where(ut.id, isEqualTo(id))
                .build().render(RenderingStrategies.MYBATIS3))
            .collect(Collectors.toList());

        SqlResult updBatch = db.updateBatch(updateStmts);
        System.out.println("updateBatch: " + updBatch.rowsAffected());

        // ── DELETE ────────────────────────────────────────────────────────────

        System.out.println("delete bob: " + users.delete(bobId));

        // ── TRANSACTION — functional ──────────────────────────────────────────

        Long orderId = db.transact(tx -> {
            GeneralInsertStatementProvider ins = insertInto(ot)
                .set(ot.userId).toValue(aliceId)
                .set(ot.product).toValue("Laptop")
                .set(ot.amount).toValue(new BigDecimal("999.99"))
                .set(ot.status).toValue("PENDING")
                .set(ot.createdAt).toValue(LocalDateTime.now())
                .build().render(RenderingStrategies.MYBATIS3);

            Long newId = db.insert(tx, ins).generatedKey(Long.class);
            db.rawExecute(tx,
                "UPDATE users SET balance = balance - ? WHERE id = ?",
                new BigDecimal("50"), aliceId);
            return newId;
        });
        System.out.println("\ntransact: orderId=" + orderId);

        // ── TRANSACTION — manual + savepoint ─────────────────────────────────

        try (TransactionScope tx = db.transaction()) {
            db.rawExecute(tx,
                "INSERT INTO orders (user_id, product, amount, status, created_at)"
                    + " VALUES (?, ?, ?, 'PENDING', NOW())",
                aliceId, "Phone", new BigDecimal("299.00"));

            java.sql.Savepoint sp = tx.savepoint("before_balance");
            try {
                db.rawExecute(tx,
                    "UPDATE users SET balance = balance - 999999 WHERE id = ?", aliceId);
                long neg = db.rawSelectLong(
                    "SELECT COUNT(*) FROM users WHERE id = ? AND balance < 0", aliceId);
                if (neg > 0) throw new DynSql.DynSqlException("Insufficient balance");
            } catch (DynSql.DynSqlException e) {
                tx.rollbackTo(sp);
                System.out.println("savepoint rollback: " + e.getMessage());
            }
            tx.commit();
            System.out.println("manual tx committed");
        } catch (Exception e) {
            System.out.println("tx failed: " + e.getMessage());
        }

        // ── STREAM ────────────────────────────────────────────────────────────

        int[] count = {0};
        users.streamAll(u -> count[0]++);
        System.out.println("\nstream: " + count[0] + " users");

        // ── RAW SQL — JOIN ────────────────────────────────────────────────────

        List<String> lines = db.rawSelectList(
            "SELECT u.name AS user_name, o.product"
                + " FROM users u JOIN orders o ON o.user_id = u.id"
                + " WHERE o.status = ? ORDER BY o.created_at DESC",
            rs -> rs.getString("user_name") + " → " + rs.getString("product"),
            "PENDING"
        );
        System.out.println("\npending orders:");
        lines.forEach(l -> System.out.println("  " + l));

        // ── ADVANCED WHERE ────────────────────────────────────────────────────

        List<User> ranged = db.selectList(
            select(ut.id, ut.name, ut.email, ut.status, ut.balance, ut.createdAt)
                .from(ut)
                .where(ut.balance, isBetween(BigDecimal.ZERO).and(new BigDecimal("500")))
                .and(ut.status, isNotNull())
                .orderBy(ut.balance.descending())
                .build().render(RenderingStrategies.MYBATIS3),
            USER_MAPPER
        );
        System.out.println("balance 0-500: " + ranged.size());

        System.out.println("\ncache size: " + db.cacheSize());
        System.out.println("Done.");
    }
}
