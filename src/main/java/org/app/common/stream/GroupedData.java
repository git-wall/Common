package org.app.common.stream;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

// Helper classes for grouping and joining
@Getter
class GroupedData<K, T> {
    private final K key;
    private final List<T> items;

    public GroupedData(K key, List<T> items) {
        this.key = key;
        this.items = new ArrayList<>(items);
    }

    public int count() { return items.size(); }

    @Override
    public String toString() {
        return String.format("Group{key=%s, count=%d}", key, count());
    }
}
