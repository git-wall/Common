package org.app.common.algorithms;

import org.springframework.lang.NonNull;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A memory-efficient set implementation for generic objects.
 * Uses open addressing with linear probing instead of separate chaining
 * to reduce memory overhead compared to standard HashSet implementations.
 *
 * @param <T> The type of elements in this set
 */
public class CompactSet<T> implements Iterable<T> {

    // Use an array instead of linked nodes to save memory
    private Object[] elements;
    private boolean[] occupied;
    private boolean[] deleted;
    private int size;
    private int capacity;
    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    /**
     * Creates a compact set with default capacity.
     */
    public CompactSet() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a compact set with the specified capacity.
     *
     * @param capacity the initial capacity
     */
    public CompactSet(int capacity) {
        this.capacity = capacity;
        this.elements = new Object[capacity];
        this.occupied = new boolean[capacity];
        this.deleted = new boolean[capacity];
        this.size = 0;
    }

    /**
     * Creates a compact set containing elements from the given collection.
     *
     * @param collection the collection whose elements are to be placed into this set
     */
    public CompactSet(Collection<? extends T> collection) {
        this(Math.max((int) ((float) collection.size() / LOAD_FACTOR) + 1, DEFAULT_CAPACITY));
        addAll(collection);
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * Resizes the internal storage if needed to maintain efficiency.
     *
     * @param element the element to be added to this set
     * @return true if this set did not already contain the specified element
     */
    public boolean add(T element) {
        if ((float) size >= (float) capacity * LOAD_FACTOR) {
            resize(capacity << 1);
        }

        int index = findPosition(element);

        if (occupied[index] && !deleted[index]) {
            return false; // Already exists
        }

        elements[index] = element;
        occupied[index] = true;
        deleted[index] = false;
        size++;
        return true;
    }

    /**
     * Adds all elements from the specified collection to this set.
     *
     * @param collection collection containing elements to be added to this set
     * @return true if this set changed as a result of the call
     */
    public boolean addAll(Collection<? extends T> collection) {
        boolean modified = false;
        for (T element : collection) {
            if (add(element)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Removes the specified element from this set if it is present.
     * Uses lazy deletion to mark slots as deleted without immediate cleanup.
     *
     * @param element the element to be removed from this set, if present
     * @return true if this set contained the specified element
     */
    public boolean remove(T element) {
        int index = findPosition(element);

        if (!occupied[index] || deleted[index]) {
            return false; // Doesn't exist
        }

        deleted[index] = true;
        size--;

        // If more than half of the slots are deleted, perform cleanup
        if (countDeleted() > capacity / 2) {
            cleanup();
        }

        return true;
    }

    /**
     * Removes all elements in this set that are also contained in the specified collection.
     *
     * @param collection collection containing elements to be removed from this set
     * @return true if this set changed as a result of the call
     */
    public boolean removeAll(Collection<T> collection) {
        boolean modified = false;
        for (T element : collection) {
            if (remove(element)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Retains only the elements in this set that are contained in the specified collection.
     *
     * @param collection collection containing elements to be retained in this set
     * @return true if this set changed as a result of the call
     */
    public boolean retainAll(Collection<?> collection) {
        boolean modified = false;

        for (T element : this) {
            if (!collection.contains(element)) {
                remove(element);
                modified = true;
            }
        }

        return modified;
    }

    /**
     * Returns true if this set contains the specified element.
     * Uses hash-based lookup with linear probing for collision resolution.
     *
     * @param element the element whose presence in this set is to be tested
     * @return true if this set contains the specified element
     */
    public boolean contains(T element) {
        int index = findPosition(element);
        return occupied[index] && !deleted[index];
    }

    /**
     * Returns true if this set contains all the elements in the specified collection.
     *
     * @param collection collection to be checked for containment in this set
     * @return true if this set contains all the elements in the specified collection
     */
    public boolean containsAll(Collection<T> collection) {
        return collection.stream().allMatch(this::contains);
    }

    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in this set
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this set contains no elements.
     *
     * @return true if this set contains no elements
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all of the elements from this set.
     * Resets all internal data structures to their initial state.
     */
    public void clear() {
        Arrays.fill(elements, null);
        Arrays.fill(occupied, false);
        Arrays.fill(deleted, false);
        size = 0;
    }

    /**
     * Returns an iterator over the elements in this set.
     * The iterator supports element traversal but not removal.
     *
     * @return an iterator over the elements in this set
     */
    @Override
    @NonNull
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int currentIndex = 0;
            private int returnedCount = 0;

            @Override
            public boolean hasNext() {
                return returnedCount < size;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                while (currentIndex < capacity) {
                    if (occupied[currentIndex] && !deleted[currentIndex]) {
                        @SuppressWarnings("unchecked")
                        T element = (T) elements[currentIndex++];
                        returnedCount++;
                        return element;
                    }
                    currentIndex++;
                }

                throw new NoSuchElementException();
            }
        };
    }

    /**
     * Returns a sequential Stream with this set as its source.
     *
     * @return a sequential Stream over the elements in this set
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    /**
     * Returns a parallel Stream with this set as its source.
     *
     * @return a parallel Stream over the elements in this set
     */
    public Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    /**
     * Creates a Spliterator over the elements in this set.
     *
     * @return a Spliterator over the elements in this set
     */
    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(
                iterator(), size, Spliterator.DISTINCT | Spliterator.SIZED);
    }

    /**
     * Returns all elements in this set as an array.
     *
     * @return an array containing all the elements in this set
     */
    public Object[] toArray() {
        Object[] result = new Object[size];
        int index = 0;

        for (int i = 0; i < capacity; i++) {
            if (occupied[i] && !deleted[i]) {
                result[index++] = elements[i];
            }
        }

        return result;
    }

    /**
     * Returns an array containing all of the elements in this set;
     * the runtime type of the returned array is that of the specified array.
     *
     * @param a the array into which the elements of this set are to be stored
     * @return an array containing all the elements in this set
     */
    @SuppressWarnings("unchecked")
    public <E> E[] toArray(E[] a) {
        if (a.length < size) {
            a = Arrays.copyOf(a, size);
        }

        int index = 0;
        for (int i = 0; i < capacity; i++) {
            if (occupied[i] && !deleted[i]) {
                a[index++] = (E) elements[i];
            }
        }

        if (a.length > size) {
            a[size] = null;
        }

        return a;
    }

    /**
     * Finds the position for an element using hash-based lookup with linear probing.
     * Used by add, contains, and remove methods to locate elements.
     */
    private int findPosition(T element) {
        int hash = getHash(element);
        int index = hash % capacity;
        int startIndex = index;

        // Linear probing
        while (occupied[index]) {
            boolean equals = Objects.equals(elements[index], element);
            if (!(deleted[index] || !equals)) break;
            index = (index + 1) % capacity;
            if (index == startIndex) {
                // This should never happen with proper resizing
                throw new IllegalStateException("Set is full");
            }
        }

        return index;
    }

    /**
     * Gets the hash code for an element, handling null values.
     * Returns a positive integer for consistent indexing.
     */
    private int getHash(T element) {
        return element == null ? 0 : Math.abs(element.hashCode());
    }

    /**
     * Counts the number of deleted slots to determine when cleanup is needed.
     */
    private int countDeleted() {
        int count = 0;
        for (boolean isDel : deleted) {
            if (isDel) {
                count++;
            }
        }
        return count;
    }

    /**
     * Resizes the set to the new capacity.
     * Rehashes all existing elements to maintain proper positioning.
     */
    private void resize(int newCapacity) {
        Object[] oldElements = elements;
        boolean[] oldOccupied = occupied;
        boolean[] oldDeleted = deleted;
        int oldCapacity = capacity;

        elements = new Object[newCapacity];
        occupied = new boolean[newCapacity];
        deleted = new boolean[newCapacity];
        capacity = newCapacity;
        size = 0;

        // Rehash all existing elements
        for (int i = 0; i < oldCapacity; i++) {
            if (oldOccupied[i] && !oldDeleted[i]) {
                @SuppressWarnings("unchecked")
                T element = (T) oldElements[i];
                add(element);
            }
        }
    }

    /**
     * Cleans up the set by removing deleted markers and rehashing.
     * Called when too many slots are marked as deleted.
     */
    private void cleanup() {
        resize(capacity);
    }

    /**
     * Returns a string representation of this set.
     *
     * @return a string representation of this set
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;

        for (int i = 0; i < capacity; i++) {
            if (occupied[i] && !deleted[i]) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(elements[i]);
                first = false;
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Compares the specified object with this set for equality.
     *
     * @param o object to be compared for equality with this set
     * @return true if the specified object is equal to this set
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompactSet)) return false;

        @SuppressWarnings("unchecked")
        CompactSet<Object> that = (CompactSet<Object>) o;
        if (size != that.size) return false;

        // Check if all elements in this set are in the other set

        return IntStream.range(0, capacity)
                .filter(i -> occupied[i] && !deleted[i])
                .allMatch(i -> that.contains(elements[i]));
    }

    /**
     * Returns the hash code value for this set.
     *
     * @return the hash code value for this set
     */
    @Override
    public int hashCode() {
        return IntStream.range(0, capacity)
                .filter(i -> occupied[i] && !deleted[i])
                .map(i -> (elements[i] == null ? 0 : elements[i].hashCode()))
                .sum();
    }
}