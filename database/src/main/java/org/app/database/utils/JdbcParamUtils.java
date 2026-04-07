package org.app.database.utils;

import java.math.BigDecimal;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class JdbcParamUtils {

    @FunctionalInterface
    public interface SqlSetter {
        void set(PreparedStatement ps, int index, Object value) throws SQLException;
    }

    private static final Map<Class<?>, SqlSetter> SETTERS = new HashMap<>();

    static {
        SETTERS.put(String.class, (ps,i,v) -> ps.setString(i,(String)v));
        SETTERS.put(Long.class, (ps,i,v) -> ps.setLong(i,(Long)v));
        SETTERS.put(Integer.class, (ps,i,v) -> ps.setInt(i,(Integer)v));
        SETTERS.put(Boolean.class, (ps,i,v) -> ps.setBoolean(i,(Boolean)v));
        SETTERS.put(Double.class, (ps,i,v) -> ps.setDouble(i,(Double)v));
        SETTERS.put(Float.class, (ps,i,v) -> ps.setFloat(i,(Float)v));
        SETTERS.put(Short.class, (ps,i,v) -> ps.setShort(i,(Short)v));

        SETTERS.put(BigDecimal.class, (ps, i, v) -> ps.setBigDecimal(i,(BigDecimal)v));
        SETTERS.put(byte[].class, (ps,i,v) -> ps.setBytes(i,(byte[])v));

        SETTERS.put(LocalDate.class, (ps,i,v) -> ps.setDate(i, Date.valueOf((LocalDate)v)));
        SETTERS.put(LocalDateTime.class, (ps, i, v) -> ps.setTimestamp(i, Timestamp.valueOf((LocalDateTime)v)));
        SETTERS.put(LocalTime.class, (ps, i, v) -> ps.setTime(i, Time.valueOf((LocalTime)v)));
        SETTERS.put(Instant.class, (ps, i, v) -> ps.setTimestamp(i, Timestamp.from((Instant)v)));

        SETTERS.put(java.util.UUID.class, (ps,i,v) -> ps.setString(i,v.toString()));

        SETTERS.put(Timestamp.class, (ps,i,v) -> ps.setTimestamp(i,(Timestamp)v));
        SETTERS.put(Date.class, (ps,i,v) -> ps.setDate(i,(Date)v));
        SETTERS.put(Time.class, (ps,i,v) -> ps.setTime(i,(Time)v));
    }

    public static void setParam(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.NULL);
            return;
        }

        if (value instanceof Enum<?>) {
            ps.setString(index, ((Enum<?>) value).name());
            return;
        }

        SqlSetter setter = SETTERS.get(value.getClass());

        if (setter != null) {
            setter.set(ps, index, value);
        } else {
            ps.setObject(index, value); // fallback
        }
    }
}
