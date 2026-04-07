package org.app.common.module.benchmark.aggregate;

public class AggStat {

    public long count = 0;

    public double avgTime = 0;
    public double avgMem = 0;

    public long minTime = Long.MAX_VALUE;
    public long minMem = Long.MAX_VALUE;

    public long maxTime = 0;
    public long maxMem = 0;

    public void merge(long timeMs, long memKb) {
        count++;

        if (count == 1) {
            avgTime = timeMs;
            avgMem = memKb;

            minTime = maxTime = timeMs;
            minMem = maxMem = memKb;
            return;
        }

        avgTime += (timeMs - avgTime) / count;
        avgMem += (memKb - avgMem) / count;

        minTime = Math.min(minTime, timeMs);
        minMem = Math.min(minMem, memKb);

        maxTime = Math.max(maxTime, timeMs);
        maxMem = Math.max(maxMem, memKb);
    }

    @Override
    public String toString() {
        return String.format(
            "count=%d, avg=(%.1fms, %.1fKB), min=(%dms, %dKB), max=(%dms, %dKB)",
            count, avgTime, avgMem, minTime, minMem, maxTime, maxMem
        );
    }
}
