package org.app.common.integration.performance.benchmark;

import lombok.Builder;
import lombok.Data;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for JMH benchmarks.
 */
@Data
@Builder
public class BenchmarkInfo {
    private final Class<?> benchmarkClass;

    @Builder.Default
    private Mode mode = Mode.AverageTime;

    @Builder.Default
    private TimeUnit timeUnit = TimeUnit.MICROSECONDS;

    @Builder.Default
    private int warmupIterations = 3;

    @Builder.Default
    private int measurementIterations = 5;

    @Builder.Default
    private int forks = 1;

    @Builder.Default
    private boolean doGC = true;

    private String resultFile;

    /**
     * Create a BenchmarkInfo from a benchmark class, respecting its annotations
     */
    public static BenchmarkInfo fromClass(Class<?> benchmarkClass) {
        BenchmarkInfoBuilder builder = BenchmarkInfo.builder()
                .benchmarkClass(benchmarkClass);

        // Check for BenchmarkMode annotation
        if (benchmarkClass.isAnnotationPresent(BenchmarkMode.class)) {
            BenchmarkMode annotation = benchmarkClass.getAnnotation(BenchmarkMode.class);
            if (annotation.value().length > 0) {
                builder.mode(annotation.value()[0]);
            }
        }

        // Check for OutputTimeUnit annotation
        if (benchmarkClass.isAnnotationPresent(OutputTimeUnit.class)) {
            OutputTimeUnit annotation = benchmarkClass.getAnnotation(OutputTimeUnit.class);
            builder.timeUnit(annotation.value());
        }

        // Check for Warmup annotation
        if (benchmarkClass.isAnnotationPresent(Warmup.class)) {
            Warmup annotation = benchmarkClass.getAnnotation(Warmup.class);
            builder.warmupIterations(annotation.iterations());
        }

        // Check for Measurement annotation
        if (benchmarkClass.isAnnotationPresent(Measurement.class)) {
            Measurement annotation = benchmarkClass.getAnnotation(Measurement.class);
            builder.measurementIterations(annotation.iterations());
        }

        // Check for Fork annotation
        if (benchmarkClass.isAnnotationPresent(Fork.class)) {
            Fork annotation = benchmarkClass.getAnnotation(Fork.class);
            builder.forks(annotation.value());
        }

        return builder.build();
    }
}
