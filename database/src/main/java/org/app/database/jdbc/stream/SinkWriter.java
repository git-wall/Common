package org.app.database.jdbc.stream;

public interface SinkWriter<T> extends AutoCloseable {
    void write(T row) throws Exception;
    void flush() throws Exception;
}

