package org.app.common.action.hll;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;

@Component
public class ClickHouseWriter {

    private final JdbcTemplate jdbcTemplate;

    public ClickHouseWriter(@Qualifier("clickHouseJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void writeMonthlyVisitorStats(String table, YearMonth month, long count, Instant timestamp) {
        String sql = "INSERT INTO " + table + " (month, count, timestamp) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, month.toString(), count, Timestamp.from(timestamp));
    }

    public void writeDailyStat(String table, LocalDate date, String category, long value) {
        String sql = "INSERT INTO " + table + " (date, category, value) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, date, category, value);
    }

    // Generic method for structured inserts
    public void write(String sql, Object... args) {
        jdbcTemplate.update(sql, args);
    }
}
