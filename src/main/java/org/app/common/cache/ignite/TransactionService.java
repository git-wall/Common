package org.app.common.cache.ignite;

import lombok.Getter;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteTransactions;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.transactions.TransactionConcurrency;
import org.apache.ignite.transactions.TransactionIsolation;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Service for managing transactions in Apache Ignite for OLTP workloads.
 * Provides methods for executing operations within transactions, optimistic and
 * pessimistic locking, and SQL transaction support.
 */
public class TransactionService {

    /**
     * -- GETTER --
     *  Gets the underlying Ignite instance.
     *
     */
    @Getter
    private final Ignite ignite;
    private final IgniteTransactions transactions;

    public TransactionService(Ignite ignite) {
        this.ignite = ignite;
        this.transactions = ignite.transactions();
    }

    /**
     * Executes an operation within a transaction and returns a result.
     *
     * @param txOperation The transactional operation to execute
     * @param concurrency The transaction concurrency level
     * @param isolation The transaction isolation level
     * @param <T> The return type of the operation
     * @return The result of the operation
     */
    public <T> T executeInTransaction(
            Function<Transaction, T> txOperation,
            TransactionConcurrency concurrency,
            TransactionIsolation isolation) {

        try (Transaction tx = transactions.txStart(concurrency, isolation)) {
            try {
                T result = txOperation.apply(tx);
                tx.commit();
                return result;
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    /**
     * Executes an operation within a transaction without returning a result.
     *
     * @param txOperation The transactional operation to execute
     * @param concurrency The transaction concurrency level
     * @param isolation The transaction isolation level
     */
    public void executeInTransaction(
            Consumer<Transaction> txOperation,
            TransactionConcurrency concurrency,
            TransactionIsolation isolation) {

        try (Transaction tx = transactions.txStart(concurrency, isolation)) {
            try {
                txOperation.accept(tx);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw e;
            }
        }
    }

    /**
     * Executes an operation within a pessimistic transaction.
     *
     * @param txOperation The transactional operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     */
    public <T> T executeInPessimisticTransaction(Function<Transaction, T> txOperation) {
        return executeInTransaction(
                txOperation,
                TransactionConcurrency.PESSIMISTIC,
                TransactionIsolation.REPEATABLE_READ);
    }

    /**
     * Executes an operation within an optimistic transaction.
     *
     * @param txOperation The transactional operation to execute
     * @param <T> The return type of the operation
     * @return The result of the operation
     */
    public <T> T executeInOptimisticTransaction(Function<Transaction, T> txOperation) {
        return executeInTransaction(
                txOperation,
                TransactionConcurrency.OPTIMISTIC,
                TransactionIsolation.SERIALIZABLE);
    }

//    /**
//     * Executes a SQL query within a transaction.
//     *
//     * @param query The SQL query to execute
//     * @param args The arguments for the SQL query
//     * @return The result of the SQL query
//     */
//    public List<List<?>> executeSqlQueryInTransaction(String query, Object... args) {
//        return executeInPessimisticTransaction(tx -> {
//            SqlFieldsQuery sqlQuery = new SqlFieldsQuery(query);
//
//            if (args != null && args.length > 0) {
//                sqlQuery.setArgs(args);
//            }
//
//            return ignite.context().query().querySqlFields(sqlQuery, true).getAll();
//        });
//    }
//
//    /**
//     * Executes an atomic put operation within a transaction.
//     *
//     * @param cacheName The name of the cache
//     * @param key The key
//     * @param value The value
//     * @param <K> The key type
//     * @param <V> The value type
//     */
//    public <K, V> void putInTransaction(String cacheName, K key, V value) {
//        executeInPessimisticTransaction(tx -> {
//            IgniteCache<K, V> cache = ignite.cache(cacheName);
//            cache.put(key, value);
//        });
//    }
//
//    /**
//     * Executes an atomic remove operation within a transaction.
//     *
//     * @param cacheName The name of the cache
//     * @param key The key to remove
//     * @param <K> The key type
//     */
//    public <K> void removeInTransaction(String cacheName, K key) {
//        executeInPessimisticTransaction(tx -> {
//            IgniteCache<K, Object> cache = ignite.cache(cacheName);
//            cache.remove(key);
//        });
//    }
//
//    /**
//     * Executes a bulk put operation within a transaction.
//     *
//     * @param cacheName The name of the cache
//     * @param dataMap The data to put into the cache
//     * @param <K> The key type
//     * @param <V> The value type
//     */
//    public <K, V> void putAllInTransaction(String cacheName, java.util.Map<K, V> dataMap) {
//        executeInPessimisticTransaction(tx -> {
//            IgniteCache<K, V> cache = ignite.cache(cacheName);
//            cache.putAll(dataMap);
//        });
//    }

    /**
     * Begins a new multi-operation transaction.
     *
     * @param concurrency The transaction concurrency level
     * @param isolation The transaction isolation level
     * @return The transaction object
     */
    public Transaction beginTransaction(
            TransactionConcurrency concurrency,
            TransactionIsolation isolation) {
        return transactions.txStart(concurrency, isolation);
    }

}
