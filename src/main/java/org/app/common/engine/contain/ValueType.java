package org.app.common.engine.contain;

import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public enum ValueType {
    INTEGER, DOUBLE, STRING, BOOLEAN, DATETIME;

    public static final Map<ValueType, Function<Object, ?>> VALUE_TYPE_FUNCTIONS;

    static {
        VALUE_TYPE_FUNCTIONS = Map.of(
                INTEGER, value -> (value instanceof Number) ? ((Number) value).intValue() : Integer.parseInt(value.toString()),
                DOUBLE, value -> (value instanceof Number) ? ((Number) value).doubleValue() : Double.parseDouble(value.toString()),
                STRING, Object::toString,
                BOOLEAN, value -> (value instanceof Boolean) ? value : Boolean.valueOf(value.toString()),
                DATETIME, ValueType::toInstant
        );
    }

    public static Object normalize(ValueType valueType, Object value) {
        if (value == null) return null;

        Function<Object, ?> function = VALUE_TYPE_FUNCTIONS.get(valueType);

        if (function == null) {
            throw new UnsupportedOperationException("Value type not supported: " + valueType);
        }

        return function.apply(value);
    }

    public static Instant toInstant(Object input) {
        if (input instanceof Instant) return (Instant) input;
        if (input instanceof LocalDateTime) {
            return ((LocalDateTime) input).atZone(ZoneId.systemDefault()).toInstant();
        }
        if (input instanceof LocalTime) {
            // optional: choose today's date or fixed reference date
            return ((LocalTime) input)
                    .atDate(LocalDate.now())
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
        }
        if (input instanceof java.util.Date) {
            return ((Date) input).toInstant();
        }
        if (input instanceof String) {
            String str = (String) input;
            try {
                return Instant.parse(str); // try full ISO
            } catch (Exception e) {
                // fallback: try LocalDateTime
                return LocalDateTime.parse(str)
                        .atZone(ZoneId.systemDefault())
                        .toInstant();
            }
        }
        if (input instanceof Long) {
            return Instant.ofEpochMilli((Long) input);
        }
        throw new IllegalArgumentException("Unsupported time input: " + input);
    }
}
