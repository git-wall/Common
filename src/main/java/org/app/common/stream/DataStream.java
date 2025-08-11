package org.app.common.stream;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataStream<T> {
    private final List<T> data;
    private final List<Function<List<T>, List<T>>> operations;

    private DataStream(Collection<T> data) {
        this.data = new ArrayList<>(data);
        this.operations = new ArrayList<>();
    }

    // Factory methods
    public static <T> DataStream<T> FROM(Collection<T> data) {
        return new DataStream<>(data);
    }

    @SafeVarargs
    public static <T> DataStream<T> of(T... items) {
        return new DataStream<>(Arrays.asList(items));
    }

    // SELECT operations
    @SuppressWarnings("unchecked")
    public <R> DataStream<R> SELECT(Function<T, R> mapper) {
        DataStream<R> result = new DataStream<>(Collections.emptyList());
        result.operations.addAll(this.operations.stream()
                .map(op -> (Function<List<R>, List<R>>) list ->
                        op.apply((List<T>) list).stream()
                                .map(mapper)
                                .collect(Collectors.toList()))
                .collect(Collectors.toList()));

        result.operations.add(list ->
                this.execute().stream()
                        .map(mapper)
                        .collect(Collectors.toList()));

        return result;
    }

    public DataStream<Map<String, Object>> SELECT(String... fields) {
        return SELECT(item -> {
            Map<String, Object> result = new HashMap<>();
            if (item instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) item;
                for (String field : fields) {
                    result.put(field, map.get(field));
                }
            } else {
                // Use reflection for object fields
                try {
                    Class<?> clazz = item.getClass();
                    for (String field : fields) {
                        try {
                            var fieldObj = clazz.getDeclaredField(field);
                            fieldObj.setAccessible(true);
                            result.put(field, fieldObj.get(item));
                        } catch (Exception e) {
                            result.put(field, null);
                        }
                    }
                } catch (Exception e) {
                    result.put("value", item);
                }
            }
            return result;
        });
    }

    // Data manipulation operations
    public DataStream<T> INSERT(T item) {
        operations.add(list -> {
            List<T> result = new ArrayList<>(list);
            result.add(item);
            return result;
        });
        return this;
    }

    public DataStream<T> INSERT(Collection<T> items) {
        operations.add(list -> {
            List<T> result = new ArrayList<>(list);
            result.addAll(items);
            return result;
        });
        return this;
    }

    public DataStream<T> UPDATE(Predicate<T> condition, Function<T, T> updater) {
        operations.add(list ->
                list.stream()
                        .map(item -> condition.test(item) ? updater.apply(item) : item)
                        .collect(Collectors.toList()));
        return this;
    }

    public DataStream<T> DELETE(Predicate<T> condition) {
        operations.add(list ->
                list.stream()
                        .filter(condition.negate())
                        .collect(Collectors.toList()));
        return this;
    }

    // Filtering operations
    public DataStream<T> WHERE(Predicate<T> condition) {
        operations.add(list ->
                list.stream()
                        .filter(condition)
                        .collect(Collectors.toList()));
        return this;
    }

    public DataStream<T> WHERE(String field, Condition condition) {
        return WHERE(item -> condition.test(getFieldValue(item, field)));
    }

    // Grouping operations
    public <K> DataStream<GroupedData<K, T>> GROUP_BY(Function<T, K> keyMapper) {
        DataStream<GroupedData<K, T>> result = new DataStream<>(Collections.emptyList());

        result.operations.add(list -> {
            List<T> currentData = this.execute();
            return currentData.stream()
                    .collect(Collectors.groupingBy(keyMapper))
                    .entrySet()
                    .stream()
                    .map(entry -> new GroupedData<>(entry.getKey(), entry.getValue()))
                    .collect(Collectors.toList());
        });

        return result;
    }

    public DataStream<GroupedData<Object, T>> GROUP_BY(String field) {
        return GROUP_BY(item -> getFieldValue(item, field));
    }

    public <G> DataStream<G> HAVING(Predicate<G> condition) {
        operations.add(list ->
                list.stream()
                        .filter(item -> condition.test((G) item))
                        .collect(Collectors.toList()));
        return (DataStream<G>) this;
    }

    // Sorting operations
    public DataStream<T> ORDER_BY(Comparator<T> comparator) {
        operations.add(list ->
                list.stream()
                        .sorted(comparator)
                        .collect(Collectors.toList()));
        return this;
    }

    public DataStream<T> ORDER_BY(Function<T, ? extends Comparable> keyExtractor) {
        return ORDER_BY(Comparator.comparing(keyExtractor));
    }

    public DataStream<T> ORDER_BY(String field) {
        return ORDER_BY(item -> (Comparable) getFieldValue(item, field));
    }

    public DataStream<T> ORDER_BY(Function<T, ? extends Comparable> keyExtractor, SortDirection direction) {
        Comparator<T> comparator = Comparator.comparing(keyExtractor);
        if (direction == SortDirection.DESC) {
            comparator = comparator.reversed();
        }
        return ORDER_BY(comparator);
    }

    // Pagination
    public DataStream<T> LIMIT(int count) {
        operations.add(list ->
                list.stream()
                        .limit(count)
                        .collect(Collectors.toList()));
        return this;
    }

    public DataStream<T> OFFSET(int count) {
        operations.add(list ->
                list.stream()
                        .skip(count)
                        .collect(Collectors.toList()));
        return this;
    }

    // Join operations
    public <U, R> DataStream<R> JOIN(Collection<U> other, BiPredicate<T, U> condition, BiFunction<T, U, R> mapper) {
        DataStream<R> result = new DataStream<>(Collections.emptyList());

        result.operations.add(list -> {
            List<T> currentData = this.execute();
            return currentData.stream()
                    .flatMap(left ->
                            other.stream()
                                    .filter(right -> condition.test(left, right))
                                    .map(right -> mapper.apply(left, right)))
                    .collect(Collectors.toList());
        });

        return result;
    }

    public <U> DataStream<JoinedData<T, U>> JOIN(Collection<U> other, BiPredicate<T, U> condition) {
        return JOIN(other, condition, JoinedData::new);
    }

    public <U, R> DataStream<R> LEFT_JOIN(Collection<U> other, BiPredicate<T, U> condition, BiFunction<T, Optional<U>, R> mapper) {
        DataStream<R> result = new DataStream<>(Collections.emptyList());

        result.operations.add(list -> {
            List<T> currentData = this.execute();
            return currentData.stream()
                    .map(left -> {
                        Optional<U> matchedRight = other.stream()
                                .filter(right -> condition.test(left, right))
                                .findFirst();
                        return mapper.apply(left, matchedRight);
                    })
                    .collect(Collectors.toList());
        });

        return result;
    }

    // Set operations
    public DataStream<T> UNION(DataStream<T> other) {
        operations.add(list -> {
            List<T> result = new ArrayList<>(list);
            result.addAll(other.execute());
            return result;
        });
        return this;
    }

    public DataStream<T> UNION_ALL(DataStream<T> other) {
        return UNION(other);
    }

    public DataStream<T> INTERSECT(DataStream<T> other) {
        operations.add(list -> {
            Set<T> otherSet = new HashSet<>(other.execute());
            return list.stream()
                    .filter(otherSet::contains)
                    .collect(Collectors.toList());
        });
        return this;
    }

    public DataStream<T> EXCEPT(DataStream<T> other) {
        operations.add(list -> {
            Set<T> otherSet = new HashSet<>(other.execute());
            return list.stream()
                    .filter(item -> !otherSet.contains(item))
                    .collect(Collectors.toList());
        });
        return this;
    }

    // Utility operations
    public DataStream<T> DISTINCT() {
        operations.add(list ->
                list.stream()
                        .distinct()
                        .collect(Collectors.toList()));
        return this;
    }

    public DataStream<T> DISTINCT(Function<T, ?> keyExtractor) {
        operations.add(list -> {
            Set<Object> seen = new HashSet<>();
            return list.stream()
                    .filter(item -> seen.add(keyExtractor.apply(item)))
                    .collect(Collectors.toList());
        });
        return this;
    }

    // Aggregate functions
    public long COUNT() {
        return execute().size();
    }

    public OptionalDouble SUM(Function<T, Number> mapper) {
        return execute().stream()
                .mapToDouble(item -> mapper.apply(item).doubleValue())
                .reduce(Double::sum);
    }

    public OptionalDouble AVG(Function<T, Number> mapper) {
        return execute().stream()
                .mapToDouble(item -> mapper.apply(item).doubleValue())
                .average();
    }

    public <R extends Comparable<R>> Optional<R> MIN(Function<T, R> mapper) {
        return execute().stream()
                .map(mapper)
                .min(Comparator.naturalOrder());
    }

    public <R extends Comparable<R>> Optional<R> MAX(Function<T, R> mapper) {
        return execute().stream()
                .map(mapper)
                .max(Comparator.naturalOrder());
    }

    // Existence operations
    public boolean EXISTS(Predicate<T> condition) {
        return execute().stream().anyMatch(condition);
    }

    public boolean NOT_EXISTS(Predicate<T> condition) {
        return !EXISTS(condition);
    }

    // Execution methods
    public List<T> execute() {
        return operations.stream()
                .reduce(Function.identity(), Function::andThen)
                .apply(new ArrayList<>(data));
    }

    public List<T> toList() {
        return execute();
    }

    public Set<T> toSet() {
        return new HashSet<>(execute());
    }

    public <K, V> Map<K, V> toMap(Function<T, K> keyMapper, Function<T, V> valueMapper) {
        return execute().stream()
                .collect(Collectors.toMap(keyMapper, valueMapper));
    }

    public Stream<T> stream() {
        return execute().stream();
    }

    // Utility methods
    private Object getFieldValue(T item, String field) {
        if (item instanceof Map) {
            return ((Map<?, ?>) item).get(field);
        }

        try {
            var fieldObj = item.getClass().getDeclaredField(field);
            fieldObj.setAccessible(true);
            return fieldObj.get(item);
        } catch (Exception e) {
            return null;
        }
    }
}



