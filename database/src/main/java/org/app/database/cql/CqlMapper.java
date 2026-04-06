package org.app.database.cql;

import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.data.GettableByName;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.InetAddress;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Maps {@link Row} results to Java objects.
 * <p>
 * Supports two mapping styles:
 * <ol>
 *   <li><b>Annotation-based</b> — annotate POJO fields with {@code @CqlColumn},
 *       call {@code CqlMapper.map(row, MyClass.class)}</li>
 *   <li><b>Manual lambda</b> — register a custom mapper function per class,
 *       useful for complex types or immutable records</li>
 * </ol>
 *
 * <pre>{@code
 * // ── Style 1: Annotation ───────────────────────────────────────────────────
 * public class User {
 *     @CqlMapper.CqlColumn("id")    public String id;
 *     @CqlMapper.CqlColumn("name")  public String name;
 *     @CqlMapper.CqlColumn("email") public String email;
 *     @CqlMapper.CqlColumn("age")   public int    age;
 * }
 * User user = CqlMapper.map(row, User.class);
 *
 * // ── Style 2: Lambda (recommended for records / immutable classes) ─────────
 * CqlMapper mapper = new CqlMapper();
 * mapper.register(User.class, row ->
 *     new User(row.getString("id"), row.getString("name"),
 *              row.getString("email"), row.getInt("age")));
 * User user = mapper.map(row, User.class);
 *
 * // ── Map entire ResultSet ──────────────────────────────────────────────────
 * List<User> users = mapper.mapAll(resultSet, User.class);
 * }</pre>
 */
public final class CqlMapper {

    // ── Column annotation ────────────────────────────────────────────────────

    /**
     * Maps a POJO field to a CQL column by name.
     * If omitted, the field name is used as-is (case-sensitive).
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CqlColumn {
        /** CQL column name. Defaults to field name if not specified. */
        String value() default "";
    }

    // ── Registry ─────────────────────────────────────────────────────────────

    private final Map<Class<?>, Function<Row, ?>> registry    = new ConcurrentHashMap<>();
    private final Map<Class<?>, List<FieldMapping>> fieldCache = new ConcurrentHashMap<>();

    // ── Static convenience (annotation-only, no registry) ────────────────────

    /**
     * Map a single row to a POJO using {@code @CqlColumn} annotations.
     * Uses reflection; fields can be private. No registry needed.
     */
    public static <T> T map(Row row, Class<T> type) {
        try {
            T instance = instantiate(type);
            for (Field field : allFields(type)) {
                field.setAccessible(true);
                String col = columnName(field);
                if (hasColumn(row, col)) {
                    field.set(instance, readValue(row, col, field.getType()));
                }
            }
            return instance;
        } catch (Exception e) {
            throw new CqlMapException("Failed to map Row to " + type.getSimpleName(), e);
        }
    }

    /**
     * Map all rows of a {@link com.datastax.oss.driver.api.core.cql.ResultSet}
     * to a list using annotations.
     */
    public static <T> List<T> mapAll(com.datastax.oss.driver.api.core.cql.ResultSet rs, Class<T> type) {
        List<T> list = new ArrayList<>();
        for (Row row : rs) list.add(map(row, type));
        return list;
    }

    // ── Instance methods (with registry) ─────────────────────────────────────

    /** Register a custom mapper for a type. */
    public <T> CqlMapper register(Class<T> type, Function<Row, T> mapper) {
        registry.put(type, mapper);
        return this;
    }

    /** Map using registry if available, fallback to annotation reflection. */
    @SuppressWarnings("unchecked")
    public <T> T mapRow(Row row, Class<T> type) {
        Function<Row, ?> fn = registry.get(type);
        if (fn != null) return (T) fn.apply(row);
        return map(row, type);
    }

    public <T> List<T> mapRows(com.datastax.oss.driver.api.core.cql.ResultSet rs, Class<T> type) {
        List<T> list = new ArrayList<>();
        for (Row row : rs) list.add(mapRow(row, type));
        return list;
    }

    public <T> List<T> mapRows(com.datastax.oss.driver.api.core.cql.ResultSet rs, Function<Row, T> mapper) {
        List<T> list = new ArrayList<>();
        for (Row row : rs)
            list.add(mapper.apply(row));
        return list;
    }

    // ── Row reading helpers (public — useful in manual mappers) ──────────────

    public static String  str(Row row, String col)  { return row.getString(col); }
    public static int     i32(Row row, String col)  { return row.getInt(col); }
    public static long    i64(Row row, String col)  { return row.getLong(col); }
    public static double  dbl(Row row, String col)  { return row.getDouble(col); }
    public static float   flt(Row row, String col)  { return row.getFloat(col); }
    public static boolean bool(Row row, String col) { return row.getBoolean(col); }
    public static UUID    uuid(Row row, String col) { return row.getUuid(col); }
    public static Instant instant(Row row, String col) { return row.getInstant(col); }
    public static LocalDate date(Row row, String col)  { return row.getLocalDate(col); }
    public static LocalTime time(Row row, String col)  { return row.getLocalTime(col); }
    public static BigDecimal decimal(Row row, String col) { return row.getBigDecimal(col); }
    public static BigInteger bigint(Row row, String col)  { return row.getBigInteger(col); }
    public static InetAddress inet(Row row, String col)   { return row.getInetAddress(col); }
    public static byte[] bytes(Row row, String col) {
        java.nio.ByteBuffer bb = row.getByteBuffer(col);
        if (bb == null) return new byte[0];
        byte[] b = new byte[bb.remaining()];
        bb.get(b);
        return b;
    }

    public static <T> List<T>        list(Row row, String col, Class<T> t)  { return row.getList(col, t); }
    public static <T> Set<T>         set(Row row, String col, Class<T> t)   { return row.getSet(col, t); }
    public static <K, V> Map<K, V>   map(Row row, String col, Class<K> k, Class<V> v) { return row.getMap(col, k, v); }

    /** Returns null if column is CQL null, otherwise maps via getter. */
    public static <T> Optional<T> opt(Row row, String col, Function<String, T> getter) {
        if (row.isNull(col)) return Optional.empty();
        return Optional.ofNullable(getter.apply(col));
    }

    // ── Internals ─────────────────────────────────────────────────────────────

    private static String columnName(Field f) {
        CqlColumn ann = f.getAnnotation(CqlColumn.class);
        if (ann != null && !ann.value().isBlank()) return ann.value();
        return f.getName();
    }

    private static boolean hasColumn(Row row, String col) {
        try { row.getColumnDefinitions().get(col); return true; }
        catch (Exception e) { return false; }
    }

    static Map<Class<?>, BiFunction<Row, String, ?>> typeFunc = new HashMap<>();

    static {
        typeFunc.put(String.class, GettableByName::getString);
        typeFunc.put(int.class, GettableByName::getInt);
        typeFunc.put(Integer.class, GettableByName::getInt);
        typeFunc.put(long.class, GettableByName::getLong);
        typeFunc.put(Long.class, GettableByName::getLong);
        typeFunc.put(double.class, GettableByName::getDouble);
        typeFunc.put(Double.class, GettableByName::getDouble);
        typeFunc.put(float.class, GettableByName::getFloat);
        typeFunc.put(Float.class, GettableByName::getFloat);
        typeFunc.put(boolean.class, GettableByName::getBoolean);
        typeFunc.put(Boolean.class, GettableByName::getBoolean);
        typeFunc.put(UUID.class, GettableByName::getUuid);
        typeFunc.put(Instant.class, GettableByName::getInstant);
        typeFunc.put(LocalDate.class, GettableByName::getLocalDate);
        typeFunc.put(LocalTime.class, GettableByName::getLocalTime);
        typeFunc.put(BigDecimal.class, GettableByName::getBigDecimal);
        typeFunc.put(BigInteger.class, GettableByName::getBigInteger);
        typeFunc.put(InetAddress.class, GettableByName::getInetAddress);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object readValue(Row row, String col, Class<?> type) {
        if (row.isNull(col)) return null;
        if (type.isEnum()) {
            String s = row.getString(col);
            return s == null ? null : Enum.valueOf((Class<Enum>) type, s);
        }

        return typeFunc
            .getOrDefault(type, GettableByName::getObject)
            .apply(row, col);
    }

    private static <T> T instantiate(Class<T> type) throws Exception {
        Constructor<T> c = type.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    private static List<Field> allFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> cur = type;
        while (cur != null && cur != Object.class) {
            fields.addAll(Arrays.asList(cur.getDeclaredFields()));
            cur = cur.getSuperclass();
        }
        return fields;
    }

    private static final class FieldMapping {
        final Field  field;
        final String column;
        FieldMapping(Field field, String column) {
            this.field  = field;
            this.column = column;
        }
    }

    public static final class CqlMapException extends RuntimeException {
        private static final long serialVersionUID = 2282337092775324395L;

        public CqlMapException(String msg, Throwable cause) { super(msg, cause); }
    }
}
