package org.app.common.eav.v3;

import com.fasterxml.jackson.databind.JavaType;
import lombok.AllArgsConstructor;
import org.app.common.utils.JacksonUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.*;
import java.util.function.Function;

@AllArgsConstructor
public enum DataType {
    STRING(Object::toString),

    INT(val -> Integer.valueOf(val.toString())),
    LONG(val -> Long.valueOf(val.toString())),
    DOUBLE(val -> Double.valueOf(val.toString())),
    FLOAT(val -> Float.valueOf(val.toString())),
    BOOLEAN(val -> Boolean.valueOf(val.toString())),
    SHORT(val -> Short.valueOf(val.toString())),
    BYTE(val -> Byte.valueOf(val.toString())),
    CHAR(val -> val.toString().charAt(0)),

    BIG_DECIMAL(val -> new BigDecimal(val.toString())),
    BIG_INTEGER(val -> new BigInteger(val.toString())),

    TIME_STAMP(val -> Timestamp.valueOf(val.toString())),
    LOCAL_DATE(val -> LocalDate.parse(val.toString())),
    LOCAL_TIME(val -> LocalTime.parse(val.toString())),
    LOCAL_DATE_TIME(val -> LocalDateTime.parse(val.toString())),
    INSTANT(val -> Instant.parse(val.toString())),
    ZONED_DATE_TIME(val -> ZonedDateTime.parse(val.toString())),
    OFFSET_DATE_TIME(val -> OffsetDateTime.parse(val.toString())),

    LIST(val -> parse(val, java.util.List.class)),
    MAP(val -> parse(val, java.util.Map.class)),
    OBJ(val -> val),

    JSON(val -> JacksonUtils.readTree(val.toString()));

    private final Function<Object, Object> parser;

    public Object parse(Object value) {
        return parser.apply(value);
    }

    public static <T> T parse(Object value, Class<T> clazz) {
        return JacksonUtils.readValue(value.toString(), clazz);
    }

    public static <T> T parse(Object value, JavaType type) {
        return JacksonUtils.readValue(value.toString(), type);
    }
}
