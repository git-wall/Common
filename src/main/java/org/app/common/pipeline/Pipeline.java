package org.app.common.pipeline;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A versatile pipeline implementation for handling various data structures
 * including single elements, collections, maps, arrays, and iterables.
 *
 * @param <T> The type of elements in the pipeline
 */
public class Pipeline<T> {
    private final Iterable<T> source;
    private final boolean isEmpty;

    // Private constructor for empty pipeline
    private Pipeline() {
        this.source = Collections.emptyList();
        this.isEmpty = true;
    }

    // Private constructor for non-empty pipeline
    private Pipeline(Iterable<T> source) {
        this.source = Objects.requireNonNull(source);
        this.isEmpty = false;
    }

    /**
     * Create an empty pipeline
     */
    public static <T> Pipeline<T> empty() {
        return new Pipeline<>();
    }

    /**
     * Create a pipeline from a single element
     */
    public static <T> Pipeline<T> of(T element) {
        return element == null
                ? empty()
                : new Pipeline<>(Collections.singletonList(element));
    }

    /**
     * Create a pipeline from varargs
     */
    @SafeVarargs
    public static <T> Pipeline<T> of(T... elements) {
        if (elements == null || elements.length == 0) {
            return empty();
        }
        return new Pipeline<>(Arrays.asList(elements));
    }

    /**
     * Create a pipeline from a collection
     */
    public static <T> Pipeline<T> from(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return empty();
        }
        return new Pipeline<>(collection);
    }

    /**
     * Create a pipeline from an iterable
     */
    public static <T> Pipeline<T> from(Iterable<T> iterable) {
        if (iterable == null) {
            return empty();
        }
        return new Pipeline<>(iterable);
    }

    /**
     * Create a pipeline from a map (keys)
     */
    public static <K, V> Pipeline<K> fromKeys(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return empty();
        }
        return new Pipeline<>(map.keySet());
    }

    /**
     * Create a pipeline from a map (values)
     */
    public static <K, V> Pipeline<V> fromValues(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return empty();
        }
        return new Pipeline<>(map.values());
    }

    /**
     * Create a pipeline from a map (entries)
     */
    public static <K, V> Pipeline<Map.Entry<K, V>> fromEntries(Map<K, V> map) {
        if (map == null || map.isEmpty()) {
            return empty();
        }
        return new Pipeline<>(map.entrySet());
    }

    /**
     * Create a pipeline from a stream
     */
    public static <T> Pipeline<T> from(Stream<T> stream) {
        if (stream == null) {
            return empty();
        }
        return new Pipeline<>(stream.collect(Collectors.toList()));
    }

    /**
     * Filter elements based on predicate
     */
    public Pipeline<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty) {
            return this;
        }

        List<T> result = new ArrayList<>();
        for (T element : source) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }
        return result.isEmpty() ? empty() : new Pipeline<>(result);
    }

    /**
     * Filter elements based on predicate or throw an exception
     */
    public <X extends Throwable> Pipeline<T> filter(Predicate<? super T> predicate,
                                                    Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(exceptionSupplier);

        if (isEmpty) {
            throw exceptionSupplier.get();
        }

        List<T> result = new ArrayList<>();
        for (T element : source) {
            if (predicate.test(element)) {
                result.add(element);
            }
        }

        if (result.isEmpty()) {
            throw exceptionSupplier.get();
        }

        return new Pipeline<>(result);
    }

    /**
     * Transform elements
     */
    public <R> Pipeline<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty) {
            return empty();
        }

        List<R> result = new ArrayList<>();
        for (T element : source) {
            result.add(mapper.apply(element));
        }
        return new Pipeline<>(result);
    }

    /**
     * Transform elements that may be null
     */
    public <R> Pipeline<R> mapNotNull(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty) {
            return empty();
        }

        List<R> result = new ArrayList<>();
        for (T element : source) {
            R mapped = mapper.apply(element);
            if (mapped != null) {
                result.add(mapped);
            }
        }
        return new Pipeline<>(result);
    }

    /**
     * Flat map - transform each element into a collection and flatten
     */
    public <R> Pipeline<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty) {
            return empty();
        }

        List<R> result = new ArrayList<>();
        for (T element : source) {
            Iterable<? extends R> mapped = mapper.apply(element);
            if (mapped != null) {
                for (R r : mapped) {
                    result.add(r);
                }
            }
        }
        return new Pipeline<>(result);
    }

    /**
     * Peek - perform action on each element without modifying the pipeline
     */
    public Pipeline<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (isEmpty) {
            return this;
        }

        for (T element : source) {
            action.accept(element);
        }
        return this;
    }

    /**
     * Limit the number of elements
     */
    public Pipeline<T> limit(long maxSize) {
        if (isEmpty || maxSize <= 0L) {
            return empty();
        }

        List<T> result = new ArrayList<>();
        long count = 0L;
        for (T element : source) {
            if (count < maxSize) {
                result.add(element);
                count++;
            } else {
                break;
            }
        }
        return new Pipeline<>(result);
    }

    /**
     * Skip a number of elements
     */
    public Pipeline<T> skip(long n) {
        if (isEmpty || n <= 0L) {
            return this;
        }

        List<T> result = new ArrayList<>();
        long count = 0L;
        for (T element : source) {
            if (count < n) {
                count++;
            } else {
                result.add(element);
            }
        }
        return new Pipeline<>(result);
    }

    /**
     * Sort elements (requires elements to be Comparable)
     */
    @SuppressWarnings("unchecked")
    public Pipeline<T> sorted() {
        if (isEmpty) {
            return this;
        }

        List<T> result = new ArrayList<>();
        for (T element : source) {
            result.add(element);
        }

        result.sort((o1, o2) -> {
            if (o1 instanceof Comparable && o2 instanceof Comparable) {
                return ((Comparable<T>) o1).compareTo(o2);
            }
            throw new ClassCastException("Elements must implement Comparable");
        });

        return new Pipeline<>(result);
    }

    /**
     * Sort elements with custom comparator
     */
    public Pipeline<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator);
        if (isEmpty) {
            return this;
        }

        List<T> result = new ArrayList<>();
        for (T element : source) {
            result.add(element);
        }

        result.sort(comparator);
        return new Pipeline<>(result);
    }

    /**
     * Distinct elements (uses equals for comparison)
     */
    public Pipeline<T> distinct() {
        if (isEmpty) {
            return this;
        }

        Set<T> result = new LinkedHashSet<>();
        for (T element : source) {
            result.add(element);
        }
        return new Pipeline<>(result);
    }

    /**
     * Apply a terminal operation to collect all elements
     */
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        Objects.requireNonNull(collector);
        if (isEmpty) {
            return collector.finisher().apply(collector.supplier().get());
        }

        A container = collector.supplier().get();
        BiConsumer<A, ? super T> accumulator = collector.accumulator();
        for (T element : source) {
            accumulator.accept(container, element);
        }
        return collector.finisher().apply(container);
    }

    /**
     * Convert to list
     */
    public List<T> toList() {
        if (isEmpty) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>();
        for (T element : source) {
            result.add(element);
        }
        return result;
    }

    /**
     * Convert to set
     */
    public Set<T> toSet() {
        if (isEmpty) {
            return Collections.emptySet();
        }

        Set<T> result = new HashSet<>();
        for (T element : source) {
            result.add(element);
        }
        return result;
    }

    /**
     * Convert to map using key and value extractors
     */
    public <K, V> Map<K, V> toMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper);
        Objects.requireNonNull(valueMapper);

        if (isEmpty) {
            return Collections.emptyMap();
        }

        Map<K, V> result = new HashMap<>();
        for (T element : source) {
            K key = keyMapper.apply(element);
            V value = valueMapper.apply(element);
            result.put(key, value);
        }
        return result;
    }

    /**
     * For each terminal operation
     */
    public void forEach(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (isEmpty) {
            return;
        }

        for (T element : source) {
            action.accept(element);
        }
    }

    /**
     * Count elements
     */
    public long count() {
        if (isEmpty) {
            return 0L;
        }

        long count = 0L;
        for (T ignored : source) {
            count++;
        }
        return count;
    }

    /**
     * Find first element or return empty optional
     */
    public Optional<T> findFirst() {
        if (isEmpty) {
            return Optional.empty();
        }

        Iterator<T> iterator = source.iterator();
        return iterator.hasNext() ? Optional.ofNullable(iterator.next()) : Optional.empty();
    }

    /**
     * Find first element or return a default value
     */
    public T findFirstOrElse(T defaultValue) {
        if (isEmpty) {
            return defaultValue;
        }

        Iterator<T> iterator = source.iterator();
        return iterator.hasNext() ? iterator.next() : defaultValue;
    }

    /**
     * Find first element or throw exception
     */
    public <X extends Throwable> T findFirstOrElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier);

        if (isEmpty) {
            throw exceptionSupplier.get();
        }

        Iterator<T> iterator = source.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        throw exceptionSupplier.get();
    }

    /**
     * Check if any element matches the predicate
     */
    public boolean anyMatch(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty) {
            return false;
        }

        for (T element : source) {
            if (predicate.test(element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if all elements match the predicate
     */
    public boolean allMatch(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty) {
            return true;
        }

        for (T element : source) {
            if (!predicate.test(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if no elements match the predicate
     */
    public boolean noneMatch(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty) {
            return true;
        }

        for (T element : source) {
            if (predicate.test(element)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Performs an action on the collection as a whole, useful for modifying the collection
     * For example, adding elements to a list.
     */
    public Pipeline<T> then(Consumer<Collection<T>> action) {
        Objects.requireNonNull(action);
        if (isEmpty) {
            return this;
        }

        List<T> collection = toList();
        action.accept(collection);
        return new Pipeline<>(collection);
    }

    /**
     * Reduce elements using binary operator
     */
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        Objects.requireNonNull(accumulator);
        if (isEmpty) {
            return Optional.empty();
        }

        Iterator<T> iterator = source.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        }

        T result = iterator.next();
        while (iterator.hasNext()) {
            result = accumulator.apply(result, iterator.next());
        }
        return Optional.ofNullable(result);
    }

    /**
     * Reduce elements using identity value and binary operator
     */
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        Objects.requireNonNull(accumulator);

        if (isEmpty) {
            return identity;
        }

        T result = identity;
        for (T element : source) {
            result = accumulator.apply(result, element);
        }
        return result;
    }

    /**
     * Apply a function to the pipeline and return its result.
     * This is similar to the original 'sink' method in PipelineSeq.
     */
    public <R> Pipeline<R> sinks(Function<? super Collection<T>, Collection<R>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty) {
            return empty();
        }

        Collection<T> collection = toList();
        Collection<R> result = mapper.apply(collection);
        return result == null ? empty() : Pipeline.from(result);
    }

    /**
     * Apply a function to the pipeline and return its result.
     * This is similar to the original 'sink' method in PipelineSeq.
     */
    public <R> Pipeline<R> sink(Function<? super Collection<T>, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty) {
            return empty();
        }

        Collection<T> collection = toList();
        R result = mapper.apply(collection);
        return result == null ? empty() : Pipeline.of(result);
    }

    /**
     * Convert to Java stream
     */
    public Stream<T> stream() {
        if (isEmpty) {
            return Stream.empty();
        }

        return StreamSupport.stream(source.spliterator(), false);
    }

    /**
     * Convert to Java parallel stream
     */
    public Stream<T> parallelStream() {
        if (isEmpty) {
            return Stream.empty();
        }

        return StreamSupport.stream(source.spliterator(), true);
    }

    /**
     * Check if pipeline is empty
     */
    public boolean isEmpty() {
        return isEmpty || !source.iterator().hasNext();
    }

    /**
     * Create a new iterable from this pipeline
     */
    public Iterable<T> iterable() {
        return isEmpty ? Collections.emptyList() : source;
    }

    @Override
    public String toString() {
        if (isEmpty) {
            return "Pipeline[]";
        }

        StringBuilder sb = new StringBuilder("Pipeline[");
        Iterator<T> iterator = source.iterator();
        if (iterator.hasNext()) {
            sb.append(iterator.next());
            while (iterator.hasNext()) {
                sb.append(", ").append(iterator.next());
            }
        }
        sb.append("]");
        return sb.toString();
    }
}