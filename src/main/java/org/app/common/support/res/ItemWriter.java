package org.app.common.support.res;

import java.io.IOException;

@FunctionalInterface
public interface ItemWriter<T> {
    void write(JsonWriter json, T item) throws IOException;
}
