package org.app.common.module.benchmark.aggregate;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class VersionManager {

    public static Path nextVersion(Path apiDir) throws IOException {

        Files.createDirectories(apiDir);

        int maxVersion = findMaxVersion(apiDir);

        int nextVersion = maxVersion + 1;
        Path verDir = apiDir.resolve("v" + nextVersion);

        Files.createDirectories(verDir);

        log.info("Created version: {}", verDir);

        return verDir;
    }

    private static int findMaxVersion(Path apiDir) throws IOException {

        if (!Files.exists(apiDir)) {
            return 0;
        }

        try (Stream<Path> paths = Files.list(apiDir)) {
            return paths
                .filter(Files::isDirectory)
                .map(p -> p.getFileName().toString())
                .filter(name -> name.matches("v\\d+"))
                .mapToInt(name -> {
                    try {
                        return Integer.parseInt(name.substring(1));
                    } catch (NumberFormatException e) {
                        log.warn("Invalid version directory name: {}", name);
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        }
    }

    public static Path getLatestVersion(Path apiDir) throws IOException {

        if (!Files.exists(apiDir)) {
            return null;
        }

        int maxVersion = findMaxVersion(apiDir);

        if (maxVersion == 0) {
            return null;
        }

        return apiDir.resolve("v" + maxVersion);
    }

    public static int[] listVersions(Path apiDir) throws IOException {

        if (!Files.exists(apiDir)) {
            return new int[0];
        }

        try (Stream<Path> paths = Files.list(apiDir)) {
            return paths
                .filter(Files::isDirectory)
                .map(p -> p.getFileName().toString())
                .filter(name -> name.matches("v\\d+"))
                .mapToInt(name -> Integer.parseInt(name.substring(1)))
                .sorted()
                .toArray();
        }
    }
}
