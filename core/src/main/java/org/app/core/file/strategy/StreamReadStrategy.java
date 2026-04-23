package org.app.core.file.strategy;

import org.app.core.file.FileReadUtils;

import java.nio.file.Path;
import java.util.function.Function;

public class StreamReadStrategy implements FileAccessStrategy {

    @Override
    public void read(Path path) {
        FileReadUtils.streamLines(path, Function.identity(), System.out::println);
    }
}
