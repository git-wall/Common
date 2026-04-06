package org.app.database.jdbc.stream;

@FunctionalInterface
public interface RowHandler<T> {
    void handle(T row) throws Exception;
}
