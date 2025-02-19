package org.app.common.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public class DateTimeUtils {

    public final static String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static Timestamp timestampBy(long epochMilli) {
        try {
            String timeStamp = new SimpleDateFormat(SQL_DATE_FORMAT).format(epochMilli);
            DateFormat dateFormat = new SimpleDateFormat(SQL_DATE_FORMAT);
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
