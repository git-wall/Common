package org.app.common.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {

    public final static String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_PATTERN);
    }

    public static String format(LocalDateTime dateTime, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    public static String formatDate(LocalDate date, String format) {
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    public static String formatDateTime(LocalDateTime dateTime, String format) {
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    public static LocalDate parseDate(String date, String format) {
        return LocalDate.parse(date, DateTimeFormatter.ofPattern(format));
    }

    public static LocalDateTime parseDateTime(String dateTime, String format) {
        return LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern(format));
    }

    public static Timestamp timestampBy(long epochMilli) {
        try {
            String timeStamp = new SimpleDateFormat(DEFAULT_PATTERN).format(epochMilli);
            DateFormat dateFormat = new SimpleDateFormat(DEFAULT_PATTERN);
            dateFormat.setTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault()));
            Date date = dateFormat.parse(timeStamp);
            return new Timestamp(date.getTime());
        } catch (Exception ex) {
            return null;
        }
    }

    public static Date now(){
        return new Date(System.currentTimeMillis());
    }

    public static Date nowPlus(long time){
        return new Date(Instant.now().plusSeconds(time).toEpochMilli());
    }

    public static Date nowMinus(long time){
        return new Date(Instant.now().minusSeconds(time).toEpochMilli());
    }
}
