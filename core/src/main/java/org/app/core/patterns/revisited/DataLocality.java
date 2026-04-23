package org.app.core.patterns.revisited;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
            List<T> batch = data.subList(i, end);
            batchProcessor.accept(batch);
        }
    }
}
