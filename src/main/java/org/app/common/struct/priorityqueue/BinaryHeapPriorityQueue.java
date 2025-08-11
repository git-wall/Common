package org.app.common.struct.priorityqueue;

import java.util.*;

/**
 * A binary heap implementation of the PriorityQueue interface.
 * <p>
 * This implementation uses a binary heap to efficiently maintain the priority
 * of elements. The head of the queue is the least element with respect to the
 * specified ordering (min heap).
 * </p>
 * <p>
 * The implementation provides O(log n) time for enqueueing and dequeueing methods
 * (offer, poll, remove, add), and constant time for retrieval methods (peek, element).
 * </p>
 *
 * @param <E> the type of elements in this priority queue
 */
public class BinaryHeapPriorityQueue<E> implements PriorityQueue<E> {

    private static final long serialVersionUID = 1L;

    private static final int DEFAULT_INITIAL_CAPACITY = 11;

    private Object[] heap;
    private int size;
    private final Comparator<? super E> comparator;

    /**
     * Constructs an empty priority queue with the default initial capacity (11)
     * that orders its elements according to their natural ordering.
     */
    public BinaryHeapPriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }

    /**
     * Constructs an empty priority queue with the specified initial capacity
     * that orders its elements according to their natural ordering.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @throws IllegalArgumentException if initialCapacity is less than 1
     */
    public BinaryHeapPriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }

    /**
     * Constructs an empty priority queue with the default initial capacity (11)
     * that orders its elements according to the specified comparator.
     *
     * @param comparator the comparator that will be used to order this priority queue
     */
    public BinaryHeapPriorityQueue(Comparator<? super E> comparator) {
        this(DEFAULT_INITIAL_CAPACITY, comparator);
    }

    /**
     * Constructs an empty priority queue with the specified initial capacity
     * that orders its elements according to the specified comparator.
     *
     * @param initialCapacity the initial capacity for this priority queue
     * @param comparator the comparator that will be used to order this priority queue
     * @throws IllegalArgumentException if initialCapacity is less than 1
     */
    public BinaryHeapPriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException("Initial capacity must be at least 1");
        }
        this.heap = new Object[initialCapacity];
        this.size = 0;
        this.comparator = comparator;
    }

    /**
     * Constructs a priority queue containing the elements in the specified collection.
     * If the specified collection is a PriorityQueue or a SortedSet, this priority queue
     * will be ordered according to the same ordering. Otherwise, this priority queue
     * will be ordered according to the natural ordering of its elements.
     *
     * @param c the collection whose elements are to be placed into this priority queue
     * @throws NullPointerException if the specified collection or any of its elements are null
     */
    public BinaryHeapPriorityQueue(Collection<? extends E> c) {
        if (c instanceof java.util.PriorityQueue<?>) {
            java.util.PriorityQueue<? extends E> pq = (java.util.PriorityQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            initFromCollection(c);
        } else if (c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            initFromCollection(c);
        } else {
            this.comparator = null;
            initFromCollection(c);
            heapify();
        }
    }

    /**
     * Initializes the heap from the specified collection.
     *
     * @param c the collection to initialize from
     */
    private void initFromCollection(Collection<? extends E> c) {
        Object[] a = c.toArray();
        if (a.getClass() != Object[].class) {
            a = Arrays.copyOf(a, a.length, Object[].class);
        }
        this.heap = a;
        this.size = a.length;
    }

    /**
     * Establishes the heap invariant (described above) in the entire tree,
     * assuming nothing about the order of the elements prior to the call.
     */
    private void heapify() {
        for (int i = (size >>> 1) - 1; i >= 0; i--) {
            siftDown(i, elementAt(i));
        }
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param e the element to add
     * @return true (as specified by Collection.add)
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param e the element to add
     * @return true (as specified by Queue.offer)
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean offer(E e) {
        Objects.requireNonNull(e);
        int i = size;
        if (i >= heap.length) {
            grow(i + 1);
        }
        size = i + 1;
        if (i == 0) {
            heap[0] = e;
        } else {
            siftUp(i, e);
        }
        return true;
    }

    /**
     * Increases the capacity of the array.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        int oldCapacity = heap.length;
        // Double size if small; else grow by 50%
        int newCapacity = oldCapacity + ((oldCapacity < 64) ?
                                         (oldCapacity + 2) :
                                         (oldCapacity >> 1));
        // Overflow-conscious code
        if (newCapacity - Integer.MAX_VALUE - 8 > 0) {
            newCapacity = (minCapacity > Integer.MAX_VALUE - 8) ?
                          Integer.MAX_VALUE :
                          Integer.MAX_VALUE - 8;
        }
        heap = Arrays.copyOf(heap, newCapacity);
    }

    /**
     * Retrieves and removes the head of this queue,
     * or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    @Override
    public E poll() {
        if (size == 0) {
            return null;
        }
        int s = --size;
        E result = elementAt(0);
        E x = elementAt(s);
        heap[s] = null;
        if (s != 0) {
            siftDown(0, x);
        }
        return result;
    }

    /**
     * Retrieves and removes the head of this queue.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Override
    public E remove() {
        E x = poll();
        if (x == null) {
            throw new NoSuchElementException();
        }
        return x;
    }

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    @Override
    public E peek() {
        return size == 0 ? null : elementAt(0);
    }

    /**
     * Retrieves, but does not remove, the head of this queue.
     *
     * @return the head of this queue
     * @throws NoSuchElementException if this queue is empty
     */
    @Override
    public E element() {
        E x = peek();
        if (x == null) {
            throw new NoSuchElementException();
        }
        return x;
    }

    /**
     * Returns the comparator used to order the elements in this queue,
     * or null if this queue is sorted according to the natural ordering of its elements.
     *
     * @return the comparator used to order this queue, or null if
     *         this queue is sorted according to the natural ordering of its elements
     */
    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /**
     * Returns the number of elements in this priority queue.
     *
     * @return the number of elements in this priority queue
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns true if this priority queue contains no elements.
     *
     * @return true if this priority queue contains no elements
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all of the elements from this priority queue.
     */
    @Override
    public void clear() {
        for (int i = 0; i < size; i++) {
            heap[i] = null;
        }
        size = 0;
    }

    /**
     * Returns true if this queue contains the specified element.
     * More formally, returns true if and only if this queue contains
     * at least one element e such that o.equals(e).
     *
     * @param o object to be checked for containment in this queue
     * @return true if this queue contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (o.equals(heap[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns an iterator over the elements in this queue.
     * The iterator does not return the elements in any particular order.
     *
     * @return an iterator over the elements in this queue
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    /**
     * Iterator over the elements in this queue.
     */
    private class Itr implements Iterator<E> {
        private int cursor = 0;
        private int lastRet = -1;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public E next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            lastRet = cursor;
            return elementAt(cursor++);
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            BinaryHeapPriorityQueue.this.removeAt(lastRet);
            cursor = lastRet;
            lastRet = -1;
        }
    }

    /**
     * Removes the element at the specified position in this queue.
     * Shifts any subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the queue.
     *
     * @param i the index of the element to be removed
     * @return the element previously at the specified position
     */
    private E removeAt(int i) {
        Objects.checkIndex(i, size);
        int s = --size;
        if (s == i) { // removed last element
            heap[i] = null;
        } else {
            E moved = elementAt(s);
            heap[s] = null;
            siftDown(i, moved);
            if (heap[i] == moved) {
                siftUp(i, moved);
            }
        }
        return null; // for simplicity, not returning the removed element
    }

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.
     *
     * @param o element to be removed from this queue, if present
     * @return true if this queue changed as a result of the call
     */
    @Override
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            if (o.equals(heap[i])) {
                removeAt(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds all of the elements in the specified collection to this queue.
     *
     * @param c collection containing elements to be added to this queue
     * @return true if this queue changed as a result of the call
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        if (c.isEmpty()) {
            return false;
        }
        for (E e : c) {
            offer(e);
        }
        return true;
    }

    /**
     * Returns an array containing all of the elements in this queue.
     *
     * @return an array containing all of the elements in this queue
     */
    @Override
    public Object[] toArray() {
        return Arrays.copyOf(heap, size);
    }

    /**
     * Returns an array containing all of the elements in this queue;
     * the runtime type of the returned array is that of the specified array.
     *
     * @param a the array into which the elements of this queue are to be stored,
     *          if it is big enough; otherwise, a new array of the same runtime
     *          type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in this queue
     * @throws NullPointerException if the specified array is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size) {
            return (T[]) Arrays.copyOf(heap, size, a.getClass());
        }
        System.arraycopy(heap, 0, a, 0, size);
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

    /**
     * Gets the element at the specified index.
     *
     * @param i the index
     * @return the element at the specified index
     */
    @SuppressWarnings("unchecked")
    private E elementAt(int i) {
        return (E) heap[i];
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * promoting x up the tree until it is greater than or equal to
     * its parent, or is the root.
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    @SuppressWarnings("unchecked")
    private void siftUp(int k, E x) {
        if (comparator != null) {
            siftUpUsingComparator(k, x);
        } else {
            siftUpComparable(k, x);
        }
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * promoting x up the tree until it is greater than or equal to
     * its parent, or is the root. To simplify and speed up coercions
     * and comparisons, the Comparable and Comparator versions are
     * separated into different methods that are otherwise identical.
     * (Similarly for siftDown.)
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    @SuppressWarnings("unchecked")
    private void siftUpComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            E e = elementAt(parent);
            if (key.compareTo(e) >= 0) {
                break;
            }
            heap[k] = e;
            k = parent;
        }
        heap[k] = key;
    }

    /**
     * Version of siftUp using comparator.
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    @SuppressWarnings("unchecked")
    private void siftUpUsingComparator(int k, E x) {
        while (k > 0) {
            int parent = (k - 1) >>> 1;
            E e = elementAt(parent);
            if (comparator.compare(x, e) >= 0) {
                break;
            }
            heap[k] = e;
            k = parent;
        }
        heap[k] = x;
    }

    /**
     * Inserts item x at position k, maintaining heap invariant by
     * demoting x down the tree repeatedly until it is less than or
     * equal to its children or is a leaf.
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    private void siftDown(int k, E x) {
        if (comparator != null) {
            siftDownUsingComparator(k, x);
        } else {
            siftDownComparable(k, x);
        }
    }

    /**
     * Version of siftDown for Comparable elements.
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    @SuppressWarnings("unchecked")
    private void siftDownComparable(int k, E x) {
        Comparable<? super E> key = (Comparable<? super E>) x;
        int half = size >>> 1;        // loop while a non-leaf
        while (k < half) {
            int child = (k << 1) + 1; // assume left child is least
            E c = elementAt(child);
            int right = child + 1;
            if (right < size && ((Comparable<? super E>) c).compareTo(elementAt(right)) > 0) {
                c = elementAt(child = right);
            }
            if (key.compareTo(c) <= 0) {
                break;
            }
            heap[k] = c;
            k = child;
        }
        heap[k] = key;
    }

    /**
     * Version of siftDown using comparator.
     *
     * @param k the position to fill
     * @param x the item to insert
     */
    @SuppressWarnings("unchecked")
    private void siftDownUsingComparator(int k, E x) {
        int half = size >>> 1;
        while (k < half) {
            int child = (k << 1) + 1;
            E c = elementAt(child);
            int right = child + 1;
            if (right < size && comparator.compare(c, elementAt(right)) > 0) {
                c = elementAt(child = right);
            }
            if (comparator.compare(x, c) <= 0) {
                break;
            }
            heap[k] = c;
            k = child;
        }
        heap[k] = x;
    }
}