package org.app.core.file.strategy;

import org.app.core.file.FileReadUtils;

import java.nio.file.Path;

public class MemoryReadStrategy implements FileAccessStrategy {

    @Override
    public void read(Path path) {
        String content = FileReadUtils.readSmallFile(path);
        System.out.println(content);
    }
}
