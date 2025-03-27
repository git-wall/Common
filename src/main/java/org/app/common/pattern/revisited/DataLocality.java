package org.app.common.pattern.revisited;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DataLocality<T> {
    private final List<T> data;
    private final int batchSize;

    public DataLocality(List<T> data, int batchSize) {
        this.data = new ArrayList<>(data);
        this.batchSize = batchSize;
    }

    public void processBatch(Consumer<List<T>> batchProcessor) {
        int size = data.size();
        for (int i = 0; i < size; i += batchSize) {
            int end = Math.min(i + batchSize, size);
            batchProcessor.accept(data.subList(i, end));
        }
    }

    public <R> List<R> transformBatch(Function<List<T>, List<R>> batchTransformer) {
        List<R> results = new ArrayList<>();
        processBatch(batch -> results.addAll(batchTransformer.apply(batch)));
        return results;
    }
}