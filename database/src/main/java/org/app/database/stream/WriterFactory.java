package org.app.database.stream;

import java.sql.Connection;

/**
 * Resolves a WriteStrategy + table/columns/serializer into a concrete RowWriter.
 *
 * This is the single place that wires strategy → implementation.
 * StreamPipe calls this internally when using the fast-path builder.
 */
public final class WriterFactory {

    private WriterFactory() {}

    /**
     * Build the best RowWriter for the given target connection and strategy.
     *
     * @param strategy   resolved (non-AUTO) strategy
     * @param table      target table name
     * @param columns    ordered column names
     * @param serializer converts T → Object[] of column values
     */
    public static <T> RowWriter<T> create(
            WriteStrategy strategy,
            String table,
            String[] columns,
            RowSerializer<T> serializer) {

        switch (strategy) {

            case COPY:
                return CopyWriter.of(table, columns, serializer);

            case DIRECT:
                return DirectPathWriter.of(table, columns, serializer);

            case BATCH:
            default:
                return BulkWriter.insert(table, columns, row -> {
                    try { return serializer.toColumns(row); }
                    catch (Exception e) { throw new RuntimeException(e); }
                });
        }
    }

    /**
     * Convenience: auto-detect strategy from target connection, then create writer.
     */
    public static <T> RowWriter<T> autoCreate(
            Connection targetConn,
            String table,
            String[] columns,
            RowSerializer<T> serializer) {

        WriteStrategy resolved = StrategySelector.detect(targetConn);
        return create(resolved, table, columns, serializer);
    }
}
