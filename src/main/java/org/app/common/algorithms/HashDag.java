package org.app.common.algorithms;

import lombok.AllArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

public class HashDag<E> implements Dag<E> {

    private static final long serialVersionUID = -6051230118899235327L;

    private final transient Map<E, Set<E>> map;

    private int size;

    public HashDag() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean put(E key, E value) {
        boolean added = map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        size = map.size();
        return added;
    }

    @Override
    public boolean putAll(E key, Collection<E> values) {
        Objects.requireNonNull(key, "key is null");
        if (values == null || values.isEmpty()) {
            add(key);
        } else {
            values.forEach(v -> put(key, v));
        }
        return true;
    }

    @Override
    public Deque<E> getRoots() {
        Deque<E> roots = new LinkedList<>(map.keySet());
        map.values().forEach(roots::removeAll);
        return roots;
    }

    @Override
    public int rootSize() {
        return getRoots().size();
    }

    @Override
    public Deque<E> getNodes() {
        return new LinkedList<>(map.keySet());
    }

    @Override
    public Set<E> getEdges(E key) {
        return map.get(key);
    }

    @Override
    public Deque<Edge<E>> getRootTrees() {
        return getRoots().stream()
                .map(r -> newNode(r, getChildEdges(r)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public Object[] toArray() {
        return map.keySet().toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return map.keySet().toArray(array);
    }

    @Override
    public boolean add(E e) {
        map.putIfAbsent(e, new HashSet<>());
        size = map.size();
        return true;
    }

    @Override
    public boolean remove(Object o) {
        if (map.remove(o) != null) {
            map.values().forEach(set -> set.remove(o));
            size = map.size();
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        c.forEach(this::add);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().map(this::remove).reduce(false, (a, b) -> a || b);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c, "Collection is null");
        boolean modified = map.keySet().retainAll(c);
        if (modified) {
            map.values().forEach(set -> set.retainAll(c));
            size = map.size();
        }
        return modified;
    }

    public Node<E> newNode(E data, Set<Edge<E>> children) {
        return new Node<>(data, children);
    }

    public Set<Edge<E>> getChildEdges(E parent) {
        Set<E> children = map.getOrDefault(parent, Collections.emptySet());
        if (children.isEmpty()) return Collections.emptySet();
        return children.stream()
                .map(child -> newNode(child, getChildEdges(child)))
                .collect(Collectors.toSet());
    }

    @Override
    public void clear() {
        map.clear();
        size = 0;
    }

    @AllArgsConstructor
    public static class Node<E> implements Edge<E> {
        private E data;
        private Set<Edge<E>> children;

        @Override
        public E getData() {
            return data;
        }

        @Override
        public void setData(E data) {
            this.data = data;
        }

        @Override
        public Set<Edge<E>> getChildren() {
            return children;
        }

        @Override
        public void setChildren(Set<Edge<E>> children) {
            this.children = children;
        }
    }
}
