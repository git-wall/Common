package org.app.concurrent;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Batch {

    public static <T> void processIn(List<T> list, int batchSize, Consumer<List<T>> consumer) {
        for (int i = 0; i < list.size(); i += batchSize) {
            int end = Math.min(i + batchSize, list.size());
            consumer.accept(list.subList(i, end));
        }
    }
}
