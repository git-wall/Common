package org.app.common.module.benchmark.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.app.common.module.benchmark.aggregate.AggNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public final class JsonExporter {

    private static final ObjectMapper MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private JsonExporter() {}

    public static void export(AggNode root, Path verDir) throws IOException {

        Files.createDirectories(verDir);

        exportMode(root, verDir, Mode.AVG);
        exportMode(root, verDir, Mode.MIN);
        exportMode(root, verDir, Mode.MAX);

        exportMetadata(root, verDir);

        log.info("Exported {} (count={}) to {}",
            root.method, root.stat.count, verDir);
    }

    private static void exportMode(AggNode root, Path verDir, Mode mode)
        throws IOException {

        String filename = mode.name().toLowerCase() + ".json";
        Path file = verDir.resolve(filename);

        ProfileJsonNode json = JsonMapper.toJson(root, mode);

        MAPPER.writeValue(file.toFile(), json);
    }

    private static void exportMetadata(AggNode root, Path verDir)
        throws IOException {

        Path metaFile = verDir.resolve("metadata.json");

        Metadata meta = new Metadata();
        meta.api = root.method;
        meta.count = root.stat.count;
        meta.avgTimeMs = Math.round(root.stat.avgTime);
        meta.minTimeMs = root.stat.minTime;
        meta.maxTimeMs = root.stat.maxTime;
        meta.avgMemKb = Math.round(root.stat.avgMem);
        meta.minMemKb = root.stat.minMem;
        meta.maxMemKb = root.stat.maxMem;
        meta.timestamp = System.currentTimeMillis();

        MAPPER.writeValue(metaFile.toFile(), meta);
    }

    public static class Metadata {
        public String api;
        public long count;

        public long avgTimeMs;
        public long minTimeMs;
        public long maxTimeMs;

        public long avgMemKb;
        public long minMemKb;
        public long maxMemKb;

        public long timestamp;
    }
}
