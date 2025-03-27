package org.app.common.brc;

import lombok.SneakyThrows;
import org.app.common.thread.ThreadUtils;
import sun.misc.Unsafe;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/**
 * <pre>
 *     {@code
 *          String filePath = "data.txt";
 *         List<String> processedData = readFileEfficiently(filePath, String::toUpperCase);
 *
 *         // Example of processing a list in parallel
 *         List<Integer> numbers = new ArrayList<>();
 *         numbers.add(1);
 *         numbers.add(2);
 *         numbers.add(3);
 *
 *         List<Integer> processedNumbers = processListInParallel(
 *             numbers,
 *             num -> num * 2
 *         );
 *     }
 * </pre>
 * */
public class HighPerformanceUtils {
    // Unsafe optimization for direct memory access
    private static final Unsafe unsafe;
    static {
        try {
            // Reflection to get Unsafe instance (careful with production use)
            java.lang.reflect.Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot get Unsafe instance", e);
        }
    }

    /**
     * Reads a file efficiently using buffered I/O and parallel processing
     *
     * @param filePath Path to the file
     * @param mapper Function to transform each line into an object
     * @return List of processed objects
     */
    public static <T> List<T> readFileEfficiently(String filePath, Function<String, T> mapper) {
        Path path = Paths.get(filePath);

        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .parallel()
                    .map(mapper)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file", e);
        }
    }

    /**
     * High-performance list processor using ForkJoinPool
     *
     * @param inputList List to process
     * @param processor Function to process each element
     * @return List of processed results
     */
    public static <T, R> List<R> processListInParallel(List<T> inputList, Function<T, R> processor) {
        ForkJoinPool readBull = ThreadUtils.RuntimeBuilder.forkJoinPool();

        try {
            return readBull.submit(() ->
                    inputList.parallelStream()
                            .map(processor)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            ).get();
        } catch (Exception e) {
            throw new RuntimeException("Error processing list", e);
        } finally {
            readBull.shutdown();
        }
    }

    /**
     * Ultra-fast object allocation using Unsafe (use with extreme caution)
     *
     * @param clazz Class to instantiate
     * @return Instantiated object
     */
    public static <T> T fastAllocate(Class<T> clazz) {
        try {
            return (T) unsafe.allocateInstance(clazz);
        } catch (InstantiationException e) {
            throw new RuntimeException("Cannot allocate instance", e);
        }
    }

    /**
     * Buffered reader with manual line reading for extreme performance
     *
     * @param filePath Path to the file
     * @return List of lines
     */
    @SneakyThrows
    public static List<String> ultraFastRead(String filePath) {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            return reader.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file", e);
        }
    }
}
