package org.app.database.jdbc.stream;

public interface SourceStreamer<T> {
    void stream(StreamContext ctx, RowHandler<T> handler) throws Exception;
}
