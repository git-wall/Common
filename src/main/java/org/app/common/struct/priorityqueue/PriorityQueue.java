package org.app.common.struct.priorityqueue;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * An interface for a Priority Queue data structure.
 * <p>
 * A Priority Queue is a queue where elements are ordered according to their
 * natural ordering or by a comparator provided at construction time. The head
 * of the queue is the least element with respect to the specified ordering.
 * </p>
 * <p>
 * This interface defines the basic operations that can be performed on a Priority Queue.
 * </p>
 *
 * @param <E> the type of elements in this priority queue
 */
public interface PriorityQueue<E> extends Serializable, Iterable<E> {

    /**
     * Inserts the specified element into this priority queue.
     *
     * @param e the element to add
     * @return true if the element was added to this queue, else false
     * @throws NullPointerException if the specified element is null
     */
    boolean add(E e);

    /**
     * Inserts the specified element into this priority queue.
     * This method is equivalent to {@link #add}.
     *
     * @param e the element to add
     * @return true if the element was added to this queue, else false
     * @throws NullPointerException if the specified element is null
     */
    boolean offer(E e);

    /**
     * Retrieves and removes the head of this queue,
     * or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    E poll();

    /**
     * Retrieves and removes the head of this queue.
     *
     * @return the head of this queue
     * @throws java.util.NoSuchElementException if this queue is empty
     */
    E remove();

    /**
     * Retrieves, but does not remove, the head of this queue,
     * or returns null if this queue is empty.
     *
     * @return the head of this queue, or null if this queue is empty
     */
    E peek();

    /**
     * Retrieves, but does not remove, the head of this queue.
     *
     * @return the head of this queue
     * @throws java.util.NoSuchElementException if this queue is empty
     */
    E element();

    /**
     * Returns the comparator used to order the elements in this queue,
     * or null if this queue is sorted according to the natural ordering of its elements.
     *
     * @return the comparator used to order this queue, or null if
     *         this queue is sorted according to the natural ordering of its elements
     */
    Comparator<? super E> comparator();

    /**
     * Returns the number of elements in this priority queue.
     *
     * @return the number of elements in this priority queue
     */
    int size();

    /**
     * Returns true if this priority queue contains no elements.
     *
     * @return true if this priority queue contains no elements
     */
    boolean isEmpty();

    /**
     * Removes all of the elements from this priority queue.
     */
    void clear();

    /**
     * Returns true if this queue contains the specified element.
     *
     * @param o object to be checked for containment in this queue
     * @return true if this queue contains the specified element
     */
    boolean contains(Object o);

    /**
     * Returns an iterator over the elements in this queue.
     * The iterator does not return the elements in any particular order.
     *
     * @return an iterator over the elements in this queue
     */
    @Override
    Iterator<E> iterator();

    /**
     * Removes a single instance of the specified element from this queue,
     * if it is present.
     *
     * @param o element to be removed from this queue, if present
     * @return true if this queue changed as a result of the call
     */
    boolean remove(Object o);

    /**
     * Adds all of the elements in the specified collection to this queue.
     *
     * @param c collection containing elements to be added to this queue
     * @return true if this queue changed as a result of the call
     * @throws NullPointerException if the specified collection or any
     *         of its elements are null
     */
    boolean addAll(Collection<? extends E> c);

    /**
     * Returns an array containing all of the elements in this queue.
     *
     * @return an array containing all of the elements in this queue
     */
    Object[] toArray();

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
    <T> T[] toArray(T[] a);
}