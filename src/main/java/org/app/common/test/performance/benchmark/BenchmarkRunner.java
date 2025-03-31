package org.app.common.test.performance.benchmark;

import org.openjdk.jmh.runner.RunnerException;

/**
 * Main class for running benchmarks.
 * This can be used to run all benchmarks or specific ones.
 */
public class BenchmarkRunner {

    /**
     * Run benchmarks using JMH's main method
     * This allows passing JMH command line arguments directly
     */
    public static void main(String[] args) throws Exception {
        // Otherwise, pass arguments to JMH
        org.openjdk.jmh.Main.main(args);
    }

    /**
     * Run a specific benchmark class
     */
    public static void runBenchmark(Class<?> benchmarkClass) throws RunnerException {
        BenchmarkUtils.runBenchmark(benchmarkClass);
    }

    /**
     * Run a benchmark with class options
     */
    public static void runBenchmark(BenchmarkInfo benchmarkInfo) throws RunnerException {
        BenchmarkUtils.runBenchmark(BenchmarkUtils.buildOptionsBuilder(benchmarkInfo));
    }

    /**
     * Run a specific benchmark class and save results to a file
     */
    public static void runBenchmarkWithResults(Class<?> benchmarkClass, String resultFile) throws RunnerException {
        BenchmarkUtils.runBenchmark(benchmarkClass, resultFile);
    }
    
    /**
     * Run a benchmark respecting class annotations
     */
    public static void runBenchmarkWithAnnotations(Class<?> benchmarkClass) throws RunnerException {
        BenchmarkInfo info = BenchmarkInfo.fromClass(benchmarkClass);
        runBenchmark(info);
    }
    
    /**
     * Run a benchmark respecting class annotations and save results to a file
     */
    public static void runBenchmarkWithAnnotations(Class<?> benchmarkClass, String resultFile) throws RunnerException {
        BenchmarkInfo info = BenchmarkInfo.fromClass(benchmarkClass);
        info.setResultFile(resultFile);
        runBenchmark(info);
    }
}
