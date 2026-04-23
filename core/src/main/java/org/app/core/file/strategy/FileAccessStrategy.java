package org.app.core.file.strategy;

import java.nio.file.Path;

public interface FileAccessStrategy {
    void read(Path path);
}
