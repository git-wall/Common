package org.app.common.test.performance.benchmark;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.annotation.Annotation;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for JMH benchmarking.
 * Makes it easy to run benchmarks from any project that imports this common module.
 */
public class BenchmarkUtils {

    /**
     * Run a benchmark with default settings
     * 
     * @param benchmarkClass The class containing the benchmark methods
     * @throws RunnerException If the benchmark fails to run
     */
    public static void runBenchmark(Class<?> benchmarkClass) throws RunnerException {
        runBenchmark(benchmarkClass, null);
    }

    /**
     * Run a benchmark with custom settings
     * 
     * @param benchmarkClass The class containing the benchmark methods
     * @param resultFile The file to save results to (null for no file output)
     * @throws RunnerException If the benchmark fails to run
     */
    public static void runBenchmark(Class<?> benchmarkClass, String resultFile) throws RunnerException {
        ChainedOptionsBuilder optionsBuilder = new OptionsBuilder()
                .include(benchmarkClass.getSimpleName())
                .shouldDoGC(true)
                .shouldFailOnError(true)
                .jvmArgs("-server");

        // Only set these options if the class doesn't have the corresponding annotations
        if (hasAnnotation(benchmarkClass, BenchmarkMode.class)) {
            optionsBuilder.mode(Mode.AverageTime);
        }
        
        if (hasAnnotation(benchmarkClass, OutputTimeUnit.class)) {
            optionsBuilder.timeUnit(TimeUnit.MICROSECONDS);
        }
        
        if (hasAnnotation(benchmarkClass, Warmup.class)) {
            optionsBuilder.warmupIterations(3);
        }
        
        if (hasAnnotation(benchmarkClass, Measurement.class)) {
            optionsBuilder.measurementIterations(5);
        }
        
        if (hasAnnotation(benchmarkClass, Fork.class)) {
            optionsBuilder.forks(1);
        }

        if (resultFile != null && !resultFile.isEmpty()) {
            optionsBuilder.resultFormat(ResultFormatType.JSON)
                    .result(resultFile);
        }

        new Runner(optionsBuilder.build()).run();
    }
    
    /**
     * Check if a class has a specific annotation
     */
    private static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        return !clazz.isAnnotationPresent(annotation);
    }

    /**
     * Run a benchmark with fully customizable options
     * 
     * @param optionsBuilder Pre-configured options builder
     * @throws RunnerException If the benchmark fails to run
     */
    public static void runBenchmark(ChainedOptionsBuilder optionsBuilder) throws RunnerException {
        new Runner(optionsBuilder.build()).run();
    }

    /**
     * Create a default options builder for customization
     * 
     * @param benchmarkClass The class containing the benchmark methods
     * @return A pre-configured options builder
     */
    public static ChainedOptionsBuilder buildOptionsBuilder(Class<?> benchmarkClass) {
        return new OptionsBuilder()
                .include(benchmarkClass.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MICROSECONDS)
                .warmupIterations(3)
                .measurementIterations(5)
                .forks(1);
    }

    public static ChainedOptionsBuilder buildOptionsBuilder(BenchmarkInfo benchmarkInfo) {
        return new OptionsBuilder()
                .include(benchmarkInfo.getBenchmarkClass().getSimpleName())
                .mode(benchmarkInfo.getMode())
                .timeUnit(benchmarkInfo.getTimeUnit())
                .warmupIterations(benchmarkInfo.getWarmupIterations())
                .measurementIterations(benchmarkInfo.getMeasurementIterations())
                .forks(benchmarkInfo.getForks());
    }
}