package org.app.common.design.revisited;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TableModule<K, V> {
    private final Map<K, V> records = new ConcurrentHashMap<>(32, 0.75f);
    private final Function<V, K> keyExtractor;

    public TableModule(Function<V, K> keyExtractor) {
        this.keyExtractor = keyExtractor;
    }

    public void insert(V record) {
        records.put(keyExtractor.apply(record), record);
    }

    public void update(V record) {
        K key = keyExtractor.apply(record);
        if (!records.containsKey(key)) {
            throw new IllegalArgumentException("Record not found: " + key);
        }
        records.put(key, record);
    }

    public List<V> find(Predicate<V> condition) {
        return records.values().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    public void delete(K key) {
        records.remove(key);
    }
}