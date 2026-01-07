package org.app.common.support.res;

import java.io.IOException;

public @FunctionalInterface
interface ObjectWriter {
    void write(JsonWriter json) throws IOException;
}
