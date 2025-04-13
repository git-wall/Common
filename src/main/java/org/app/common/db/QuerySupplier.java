package org.app.common.db;

import lombok.Getter;

import java.util.List;
import java.util.function.Supplier;

@Getter
public class QuerySupplier<T> {
    private Supplier<Boolean> exits;
    private Supplier<T> insert;
    private Supplier<T> select;
    private Supplier<T> update;
    private Supplier<Void> delete;
    private Supplier<List<T>> findAll;
    private Supplier<List<String>> findFields;

    public static <T> QuerySupplier<T> of() {
        return new QuerySupplier<>();
    }

    public QuerySupplier<T> exits(Supplier<Boolean> exits) {
        this.exits = exits;
        return this;
    }

    public QuerySupplier<T> insert(Supplier<T> insert) {
        this.insert = insert;
        return this;
    }

    public QuerySupplier<T> select(Supplier<T> select) {
        this.select = select;
        return this;
    }

    public QuerySupplier<T> update(Supplier<T> update) {
        this.update = update;
        return this;
    }

    public QuerySupplier<T> delete(Supplier<Void> delete) {
        this.delete = delete;
        return this;
    }

    public QuerySupplier<T> findFields(Supplier<List<String>> findFields) {
        this.findFields = findFields;
        return this;
    }

    public QuerySupplier<T> findAll(Supplier<List<T>> findAll) {
        this.findAll = findAll;
        return this;
    }
}
