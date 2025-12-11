```java
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
public class SampleBenchmark {

    @Benchmark
    public void benchmarkStringConcat(Blackhole blackhole) {
        String result = "";
        for (int i = 0; i < 100; i++) {
            result += i;
        }
        blackhole.consume(result);
    }

    @Benchmark
    public void benchmarkStringBuilder(Blackhole blackhole) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(i);
        }
        blackhole.consume(sb.toString());
    }

    /**
     * Example of how to run this benchmark directly
     */
    public static void main(String[] args) throws Exception {
        BenchmarkRunner.runBenchmark(SampleBenchmark.class);
    }
}
```