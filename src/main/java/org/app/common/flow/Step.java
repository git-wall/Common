package org.app.common.flow;



public interface Step<T> {
    T execute(T input) throws Exception;
}