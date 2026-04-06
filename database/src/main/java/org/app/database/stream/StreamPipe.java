package org.app.database.stream;

import io.streampipe.strategy.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * StreamPipe — thin, direct DB-to-DB streaming engine with auto write-strategy.
 *
 * ┌──────────────┐   JDBC cursor stream   ┌──────────────────────────────────────────┐
 * │   DB Source  │ ─────────────────────▶ │  RowMapper → [RowTransformer] → Writer   │
 * │  (read-only) │                        │           (batch flush loop)             │
 * └──────────────┘                        └──────────────┬───────────────────────────┘
 *                                                        │ AUTO strategy detection
 *                                           ┌────────────┼────────────┐
 *                                         COPY        DIRECT       BATCH
 *                                       (Postgres)   (Oracle)   (universal)
 *
 * TWO entry points:
 *
 *   1. fastLoad() — just move data as fast as possible, auto-pick best writer:
 *
 *      StreamPipe.fastLoad(targetConn, "orders", cols, row -> row.toArray())
 *          .source(sourceConn, "SELECT * FROM orders")
 *          .mapper(rs -> new OrderRow(...))
 *          .run();
 *
 *   2. builder() — full control, bring your own RowWriter:
 *
 *      StreamPipe.<OrderRow, LocalOrder>builder()
 *          .source(sourceConn, sql, params...)
 *          .mapper(rs -> ...)
 *          .transform(row -> ...)   // optional, return null to skip
 *          .target(targetConn, myWriter)
 *          .config(PipeConfig.builder().batchSize(1000).build())
 *          .run();
 */
public final class StreamPipe<S, T> {

    private static final Logger LOG = Logger.getLogger(StreamPipe.class.getName());

    private final Connection           sourceConn;
    private final String               sourceQuery;
    private final Object[]             sourceParams;
    private final RowMapper<S>         mapper;
    private final RowTransformer<S, T> transformer;
    private final Connection           targetConn;
    private final RowWriter<T>         writer;
    private final PipeConfig           config;

    private StreamPipe(Builder<S, T> b) {
        this.sourceConn   = b.sourceConn;
        this.sourceQuery  = b.sourceQuery;
        this.sourceParams = b.sourceParams;
        this.mapper       = b.mapper;
        this.transformer  = b.transformer;
        this.targetConn   = b.targetConn;
        this.writer       = b.writer;
        this.config       = b.config;
    }

    /**
     * Execute the pipe synchronously.
     * Never throws — all errors are captured in PipeResult.
     */
    public PipeResult run() {
        long startMs  = System.currentTimeMillis();
        long rowsRead = 0, written = 0, skipped = 0;

        try {
            WriteStrategy resolved = StrategySelector.resolve(config.getWriteStrategy(), targetConn);
            LOG.info(String.format("[StreamPipe] start strategy=%s fetchSize=%d batchSize=%d",
                resolved, config.getFetchSize(), config.getBatchSize()));

            sourceConn.setAutoCommit(false);
            targetConn.setAutoCommit(false);

            try (PreparedStatement ps = buildSourceStmt()) {
                ps.setFetchSize(config.getFetchSize());
                if (config.getQueryTimeoutSeconds() > 0)
                    ps.setQueryTimeout(config.getQueryTimeoutSeconds());

                try (ResultSet rs = ps.executeQuery()) {
                    List<T> batch = new ArrayList<>(config.getBatchSize());

                    while (rs.next()) {
                        rowsRead++;

                        S src = mapper.map(rs);
                        T tgt = applyTransform(src);
                        if (tgt == null) { skipped++; continue; }

                        batch.add(tgt);

                        if (batch.size() >= config.getBatchSize()) {
                            writer.write(targetConn, batch);
                            written += batch.size();
                            if (config.isCommitPerBatch()) targetConn.commit();
                            batch.clear();
                            if (rowsRead % 100_000 == 0)
                                LOG.info(String.format("[StreamPipe] progress read=%d written=%d", rowsRead, written));
                        }
                    }

                    if (!batch.isEmpty()) {
                        writer.write(targetConn, batch);
                        written += batch.size();
                        targetConn.commit();
                    }
                }
            }

            long ms = System.currentTimeMillis() - startMs;
            double rps = ms > 0 ? written * 1000.0 / ms : 0;
            LOG.info(String.format("[StreamPipe] done read=%d written=%d skipped=%d ms=%d (%.0f rows/s)",
                rowsRead, written, skipped, ms, rps));
            return PipeResult.ok(rowsRead, written, skipped, ms);

        } catch (Exception ex) {
            long ms = System.currentTimeMillis() - startMs;
            LOG.log(Level.SEVERE, "[StreamPipe] failed after read=" + rowsRead, ex);
            safeRollback(targetConn);
            return PipeResult.failed(rowsRead, written, skipped, ms, ex);
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private T applyTransform(S src) {
        return transformer != null ? transformer.transform(src) : (T) src;
    }

    private PreparedStatement buildSourceStmt() throws SQLException {
        PreparedStatement ps = sourceConn.prepareStatement(
            sourceQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
        if (sourceParams != null)
            for (int i = 0; i < sourceParams.length; i++) ps.setObject(i + 1, sourceParams[i]);
        return ps;
    }

    private void safeRollback(Connection c) {
        try { if (c != null && !c.isClosed()) c.rollback(); } catch (SQLException ignore) {}
    }

    // ══════════════════════════════════════════════════════════════════════════
    // FAST-LOAD — "just move data from A→B as fast as possible"
    // auto-selects best writer: COPY (Postgres) / DIRECT (Oracle) / BATCH (MySQL/others)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * @param targetConn  writable connection to target DB
     * @param table       target table name
     * @param columns     column names in order (must match serializer output)
     * @param serializer  lambda: row → Object[] of values
     */
    public static <T> FastLoadBuilder<T> fastLoad(
            Connection targetConn, String table, String[] columns, RowSerializer<T> serializer) {
        return new FastLoadBuilder<>(targetConn, table, columns, serializer);
    }

    public static final class FastLoadBuilder<T> {

        private final Connection       targetConn;
        private final String           table;
        private final String[]         columns;
        private final RowSerializer<T> serializer;

        private Connection   sourceConn;
        private String       sourceQuery;
        private Object[]     sourceParams;
        private RowMapper<T> mapper;
        private PipeConfig   config = PipeConfig.fullLoad();

        FastLoadBuilder(Connection tc, String table, String[] cols, RowSerializer<T> ser) {
            this.targetConn = tc; this.table = table; this.columns = cols; this.serializer = ser;
        }

        public FastLoadBuilder<T> source(Connection conn, String sql, Object... params) {
            this.sourceConn   = conn;
            this.sourceQuery  = sql;
            this.sourceParams = params.length == 0 ? null : params;
            return this;
        }

        public FastLoadBuilder<T> mapper(RowMapper<T> m)  { this.mapper  = m; return this; }
        public FastLoadBuilder<T> config(PipeConfig c)    { this.config  = c; return this; }

        /** Override auto-detected strategy. */
        public FastLoadBuilder<T> strategy(WriteStrategy s) {
            this.config = PipeConfig.builder()
                .fetchSize(config.getFetchSize()).batchSize(config.getBatchSize())
                .commitPerBatch(config.isCommitPerBatch())
                .queryTimeout(config.getQueryTimeoutSeconds())
                .strategy(s).build();
            return this;
        }

        public PipeResult run() {
            if (sourceConn == null)  throw new IllegalStateException("StreamPipe fastLoad: source() is required");
            if (sourceQuery == null) throw new IllegalStateException("StreamPipe fastLoad: source query is required");
            if (mapper == null)      throw new IllegalStateException("StreamPipe fastLoad: mapper() is required");

            WriteStrategy resolved = StrategySelector.resolve(config.getWriteStrategy(), targetConn);
            RowWriter<T>  writer   = WriterFactory.create(resolved, table, columns, serializer);

            return StreamPipe.<T, T>builder()
                .source(sourceConn, sourceQuery, sourceParams != null ? sourceParams : new Object[0])
                .mapper(mapper)
                .target(targetConn, writer)
                .config(config)
                .run();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BUILDER — full control (custom writer, transform, etc.)
    // ══════════════════════════════════════════════════════════════════════════

    public static <S, T> Builder<S, T> builder() { return new Builder<>(); }

    public static final class Builder<S, T> {

        Connection          sourceConn;
        String              sourceQuery;
        Object[]            sourceParams;
        RowMapper<S>        mapper;
        RowTransformer<S,T> transformer;
        Connection          targetConn;
        RowWriter<T>        writer;
        PipeConfig          config = PipeConfig.defaults();

        public Builder<S, T> source(Connection conn, String sql, Object... params) {
            this.sourceConn   = conn;
            this.sourceQuery  = sql;
            this.sourceParams = params.length == 0 ? null : params;
            return this;
        }

        public Builder<S, T> mapper(RowMapper<S> m)           { this.mapper       = m; return this; }
        public Builder<S, T> transform(RowTransformer<S,T> t) { this.transformer  = t; return this; }
        public Builder<S, T> config(PipeConfig c)             { this.config       = c; return this; }

        /** Full control: bring your own RowWriter. */
        public Builder<S, T> target(Connection conn, RowWriter<T> w) {
            this.targetConn = conn; this.writer = w; return this;
        }

        /**
         * Auto-strategy target: provide table + serializer, StreamPipe picks fastest writer.
         */
        public Builder<S, T> target(Connection conn, String table, String[] columns, RowSerializer<T> ser) {
            this.targetConn = conn;
            WriteStrategy s = StrategySelector.resolve(config.getWriteStrategy(), conn);
            this.writer     = WriterFactory.create(s, table, columns, ser);
            return this;
        }

        public StreamPipe<S, T> build() {
            req(sourceConn,  "source connection");
            req(sourceQuery, "source query");
            req(mapper,      "mapper");
            req(targetConn,  "target connection");
            req(writer,      "target writer — call .target(...)");
            return new StreamPipe<>(this);
        }

        public PipeResult run() { return build().run(); }

        private void req(Object v, String n) {
            if (v == null) throw new IllegalStateException("StreamPipe: " + n + " is required");
        }
    }
}
