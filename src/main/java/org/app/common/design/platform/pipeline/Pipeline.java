package org.app.common.design.platform.pipeline;

import java.util.ArrayList;
import java.util.List;

public class Pipeline<T> {
    private final List<PipelineStage<T>> stages = new ArrayList<>();

    public Pipeline<T> addStage(PipelineStage<T> stage) {
        stages.add(stage);
        return this;
    }

    public T execute(T input) {
        T result = input;
        for (PipelineStage<T> stage : stages) {
            result = stage.process(result);
        }
        return result;
    }
}
