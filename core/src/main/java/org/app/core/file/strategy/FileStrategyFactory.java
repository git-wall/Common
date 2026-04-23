package org.app.core.file.strategy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileStrategyFactory {

    private static final long SMALL_FILE = 10 * 1024 * 1024; // 10MB

    /**
     * <pre>{@code
     * Path path = Paths.get("data.txt");
     *
     * FileAccessStrategy strategy = FileStrategyFactory.getStrategy(path);
     * strategy.read(path);
     * }</pre>
     * */
    public static FileAccessStrategy getStrategy(Path path) {
        try {
            long size = Files.size(path);

            if (size < SMALL_FILE) {
                return new MemoryReadStrategy();
            } else {
                return new StreamReadStrategy();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
