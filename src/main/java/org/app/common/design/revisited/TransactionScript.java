package org.app.common.design.revisited;

import java.util.function.Function;
import java.util.function.Supplier;

public class TransactionScript<T, R> {
    private final Supplier<T> connectionProvider;
    private final Function<T, Boolean> beginTransaction;
    private final Function<T, Boolean> commitTransaction;
    private final Function<T, Boolean> rollbackTransaction;

    public TransactionScript(
            Supplier<T> connectionProvider,
            Function<T, Boolean> beginTransaction,
            Function<T, Boolean> commitTransaction,
            Function<T, Boolean> rollbackTransaction) {
        this.connectionProvider = connectionProvider;
        this.beginTransaction = beginTransaction;
        this.commitTransaction = commitTransaction;
        this.rollbackTransaction = rollbackTransaction;
    }

    public R execute(Function<T, R> script) {
        T connection = connectionProvider.get();
        try {
            beginTransaction.apply(connection);
            R result = script.apply(connection);
            commitTransaction.apply(connection);
            return result;
        } catch (Exception e) {
            rollbackTransaction.apply(connection);
            throw new RuntimeException("Transaction failed", e);
        }
    }
}