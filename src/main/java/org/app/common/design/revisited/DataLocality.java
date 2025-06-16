package org.app.common.design.revisited;

import org.app.common.entities.Action;

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
            List<T> batch = data.subList(i, end);
            batchProcessor.accept(batch);
        }
    }

    public Action processBatch(Function<List<T>, int[]> batchProcessor) {
        int size = data.size();
        Action action = new Action(size);

        for (int i = 0; i < size; i += batchSize) {
            int end = Math.min(i + batchSize, size);
            List<T> batch = data.subList(i, end);
            try {
                int[] rs = batchProcessor.apply(batch);
                action.collectDataInfo(rs);
            } catch (Exception e) {
                action.incrementUnknownSuccess(batch.size());
            }
        }
        return action;
    }
}
