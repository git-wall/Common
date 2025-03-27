package org.app.common.brc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import sun.misc.Unsafe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * <pre>
 *     {@code
 *     HighPerformancePipelineJava11<String> pipeline = new HighPerformancePipelineJava11<>()
 *             .loadMemoryMappedFile("large_input.txt")
 *             .withMapper(String::toUpperCase)
 *             .withFilter(line -> line.length() > 10)
 *             .withTransformer(line -> line.replace(" ", "_"));
 *
 *         List<?> processedResults = pipeline.process();
 *     }
 * </pre>
 * */
@Slf4j
public class HighPerformancePipeline11<T> {
    // Unsafe for low-level memory access
    private static final Unsafe unsafe;

    static {
        try {
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot get Unsafe instance", e);
        }
    }

    // Concurrent processing resources
    private final ExecutorService executorService;
    private final int processorCount;

    // Pipeline components
    private Function<String, T> mapper;
    private Predicate<T> filter;
    private Function<T, ?> transformer;

    // Memory-mapped file handling
    private ByteBuffer mappedFile;
    private long fileSize;

    public HighPerformancePipeline11() {
        this.processorCount = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newWorkStealingPool(processorCount);
    }

    /**
     * Memory-mapped file loader with direct ByteBuffer
     */
    @SneakyThrows
    public HighPerformancePipeline11<T> loadMemoryMappedFile(String filePath) {
        Path path = Paths.get(filePath);
        try (FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);) {
            // Determine file size
            this.fileSize = fileChannel.size();

            // Memory-map the entire file
            this.mappedFile = fileChannel.map(
                    FileChannel.MapMode.READ_ONLY,
                    0L,
                    fileSize
            );

            return this;
        } catch (IOException e) {
            throw new RuntimeException("Error mapping file", e);
        }
    }

    /**
     * Set mapper function for transforming input lines
     */
    public HighPerformancePipeline11<T> withMapper(Function<String, T> mapper) {
        this.mapper = mapper;
        return this;
    }

    /**
     * Set filter predicate for processing pipeline
     */
    public HighPerformancePipeline11<T> withFilter(Predicate<T> filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Set transformer for final processing stage
     */
    public HighPerformancePipeline11<T> withTransformer(Function<T, ?> transformer) {
        this.transformer = transformer;
        return this;
    }

    /**
     * Parallel processing with ForkJoinPool and CompletableFuture
     */
    public List<?> process() {
        // Validate pipeline components
        Objects.requireNonNull(mapper, "Mapper must be set");
        Objects.requireNonNull(filter, "Filter must be set");

        // Use ConcurrentHashMap for thread-safe aggregation
        ConcurrentHashMap<Long, Object> results = new ConcurrentHashMap<>();

        // Parallel processing with CompletableFuture for async handling
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Split file into chunks for parallel processing
        long chunkSize = fileSize / (long) processorCount;

        for (int i = 0; i < processorCount; i++) {
            final long start = (long) i * chunkSize;
            final long end = (i == processorCount - 1) ? fileSize : start + chunkSize;

            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> processFileChunk(start, end, results), executorService);

            futures.add(future);
        }

        // Wait for all processing to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Shutdown executor service
        executorService.shutdown();

        // Convert results to list
        return new ArrayList<>(results.values());
    }

    /**
     * Chunk processing with optimized reading
     */
    private void processFileChunk(long start, long end, ConcurrentHashMap<Long, Object> results) {
        for (long offset = start; offset < end; offset++) {
            try {
                // Read line using direct ByteBuffer
                String line = readLineOptimized(offset);

                // Skip empty lines
                if (line == null || line.trim().isEmpty()) continue;

                // Pipeline stages
                T mapped = mapper.apply(line);
                boolean passed = filter.test(mapped);

                if (passed) {
                    // Optional transformation
                    Object transformed = (transformer != null)
                            ? transformer.apply(mapped)
                            : mapped;

                    // Thread-safe result storage
                    results.put(offset, transformed);
                }
            } catch (Exception e) {
                log.error("Processing error at offset {}", offset);
            }
        }
    }

    /**
     * Optimized line reading using ByteBuffer
     */
    private String readLineOptimized(long offset) {
        // Ensure we're within file bounds
        if (offset >= fileSize) return null;

        // Set position to current offset
        mappedFile.position((int) offset);

        // Buffer for line reading
        StringBuilder lineBuilder = new StringBuilder();

        while (mappedFile.hasRemaining()) {
            char c = (char) mappedFile.get();

            // Stop at line break
            if (c == '\n' || c == '\r') {
                break;
            }

            lineBuilder.append(c);
        }

        return lineBuilder.toString().trim();
    }

    /**
     * Ultra-fast memory copy using Unsafe (use with extreme caution)
     */
    private byte[] fastMemoryCopy(long sourceAddress, int length) {
        byte[] dest = new byte[length];
        unsafe.copyMemory(
                null,
                sourceAddress,
                dest,
                Unsafe.ARRAY_BYTE_BASE_OFFSET,
                length
        );
        return dest;
    }

}
