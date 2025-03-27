package org.app.common.pattern.revisited;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SpatialPartition<T> {
    private final Map<String, List<T>> partitions = new HashMap<>();
    private final Function<T, String> partitioner;

    public SpatialPartition(Function<T, String> partitioner) {
        this.partitioner = partitioner;
    }

    public void add(T item) {
        String partition = partitioner.apply(item);
        partitions.computeIfAbsent(partition, k -> new ArrayList<>()).add(item);
    }

    public List<T> getPartition(String partition) {
        return partitions.getOrDefault(partition, new ArrayList<>());
    }

    public List<T> findNearby(String partition) {
        return new ArrayList<>(getPartition(partition));
    }
}