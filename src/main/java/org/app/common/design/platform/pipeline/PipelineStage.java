package org.app.common.design.platform.pipeline;

public interface PipelineStage<T> {
    T process(T input);
}
