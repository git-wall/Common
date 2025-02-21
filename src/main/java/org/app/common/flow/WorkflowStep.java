package org.app.common.flow;



public interface WorkflowStep<T> {
    T execute(T input) throws Exception;
}