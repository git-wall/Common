package org.app.core.file;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileReadUtils {

    public static String readSmallFile(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> readAllLines(Path path) {
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void streamLines(Path path, Function<String, T> mapper, Consumer<T> consumer) {
        try (Stream<String> lines = Files.lines(path)) {
            lines.map(mapper)
                .filter(Objects::nonNull)
                .forEach(consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
