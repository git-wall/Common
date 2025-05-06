package org.app.common.action.hll;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.Temporal;

public class HybridHyperLogLog {
    private final StringRedisTemplate redisTemplate;
    private final ClickHouseWriter clickHouseWriter;

    private static final String REDIS_KEY_FORMAT = "hll:%s:%s";

    private final long ttlDays;

    public HybridHyperLogLog(StringRedisTemplate redisTemplate, ClickHouseWriter clickHouseWriter, long ttlDays) {
        this.redisTemplate = redisTemplate;
        this.clickHouseWriter = clickHouseWriter;
        this.ttlDays = ttlDays;
    }

    public void addDailyEvent(String prefix, String value) {
        String key = formatKey(prefix, LocalDate.now());
        redisTemplate.opsForHyperLogLog().add(key, value);
        redisTemplate.expire(key, Duration.ofDays(ttlDays));
    }

    public void addMonthlyEvent(String prefix, String value) {
        String key = formatKey(prefix, YearMonth.now());
        redisTemplate.opsForHyperLogLog().add(key, value);
        redisTemplate.expire(key, Duration.ofDays(ttlDays));
    }

    public long estimate(String prefix, Temporal temporal) {
        String key = formatKey(prefix, temporal);
        return redisTemplate.opsForHyperLogLog().size(key);
    }

    // Scheduled Task for syncing Daily HLL data to ClickHouse
    @Scheduled(cron = "0 10 1 * * ?") // Every day at 01:10 AM
    public void syncDailyStatsToClickHouse() {
        LocalDate yesterday = LocalDate.now().minusDays(1L);
        String dailyKey = formatKey("visitors", yesterday);

        // Estimate the cardinality (distinct count) directly from Redis HyperLogLog
        long dailyCount = redisTemplate.opsForHyperLogLog().size(dailyKey);

        // Write the daily count to ClickHouse
        clickHouseWriter.writeDailyStat("visitors_daily", yesterday, "visitors", dailyCount);
    }

    // Scheduled Task for syncing Monthly HLL data to ClickHouse
    @Scheduled(cron = "0 5 1 * * ?") // Every 1st of the month at 01:05 AM
    public void syncMonthlyStatsToClickHouse() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1L);
        String monthlyKey = formatKey("visitors", lastMonth);
        long monthlyCount = redisTemplate.opsForHyperLogLog().size(monthlyKey);
        clickHouseWriter.writeMonthlyVisitorStats(
                "visitors_monthly",
                lastMonth,
                monthlyCount,
                Instant.now()
        );
    }

    private String formatKey(String prefix, Temporal temporal) {
        if (temporal instanceof LocalDate) {
            return String.format(REDIS_KEY_FORMAT, prefix, temporal);
        } else if (temporal instanceof YearMonth) {
            return String.format(REDIS_KEY_FORMAT, prefix, temporal);
        }
        throw new IllegalArgumentException("Unsupported temporal type: " + temporal.getClass());
    }
}
