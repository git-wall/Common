package org.app.common.algorithms.dag;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A hash-based implementation of a Directed Acyclic Graph (DAG).
 * <p>
 * This implementation stores nodes and their edges in a HashMap for efficient
 * lookup, insertion, and removal operations. It prevents cycles from forming
 * when using the putIfAcyclic method.
 * </p>
 * <p>
 * A DAG is a graph with directed edges and no cycles, making it suitable for
 * representing dependencies, workflows, or any hierarchical relationships.
 * </p>
 * 
 * @param <E> the type of elements in this DAG
 */
public class HashDag<E> implements Dag<E> {

    private static final long serialVersionUID = -6051230118899235327L;

    private final transient Map<E, Set<E>> map;

    private int size;

   /**
     * Constructs a HashDag with the specified initial capacity and load factor.
     *
     * @param initialCapacity the initial capacity of the HashMap
     * @param loadFactor the load factor of the HashMap
     */
    public HashDag(int initialCapacity, float loadFactor) {
        this.map = new HashMap<>(initialCapacity, loadFactor);
    }

    /**
     * Constructs an empty HashDag with default initial capacity and load factor.
     */
    public HashDag() {
        this.map = new HashMap<>(16, 0.75f);
    }

    /**
     * Adds a directed edge from key to value in this DAG.
     * If the key or value doesn't exist in the DAG, it will be added.
     *
     * @param key the source node
     * @param value the target node
     * @return true if the edge was added, false if it already existed
     */
    @Override
    public boolean put(E key, E value) {
        boolean added = map.computeIfAbsent(key, k -> new HashSet<>()).add(value);
        size = map.size();
        return added;
    }

    /**
     * Adds multiple directed edges from key to each value in the collection.
     * If the key or any value doesn't exist in the DAG, it will be added.
     *
     * @param key the source node
     * @param values the collection of target nodes
     * @return true if the operation was successful
     * @throws NullPointerException if key is null
     */
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

    /**
     * Returns all root nodes in this DAG.
     * A root node is a node that has no incoming edges.
     *
     * @return a deque containing all root nodes
     */
    @Override
    public Deque<E> getRoots() {
        // More efficient implementation using streams
        return map.keySet()
                .stream()
                .filter(key -> map.values().stream()
                        .noneMatch(edges -> edges.contains(key)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Returns the number of root nodes in this DAG.
     *
     * @return the number of root nodes
     */
    @Override
    public int rootSize() {
        return getRoots().size();
    }

    /**
     * Returns all nodes in this DAG.
     *
     * @return a deque containing all nodes
     */
    @Override
    public Deque<E> getNodes() {
        return new LinkedList<>(map.keySet());
    }

    /**
     * Returns all outgoing edges from the specified node.
     *
     * @param key the node whose edges to return
     * @return a set of nodes that are targets of edges from the key node
     */
    @Override
    public Set<E> getEdges(E key) {
        return map.get(key);
    }

    /**
     * Returns a tree representation of this DAG starting from root nodes.
     *
     * @return a deque of Edge objects representing the root trees
     */
    @Override
    public Deque<Edge<E>> getRootTrees() {
        return getRoots().stream()
                .map(r -> newNode(r, getChildEdges(r)))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Returns the number of nodes in this DAG.
     *
     * @return the number of nodes
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns true if this DAG contains no nodes.
     *
     * @return true if this DAG contains no nodes
     */
    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns true if this DAG contains the specified node.
     *
     * @param o the node whose presence is to be tested
     * @return true if this DAG contains the specified node
     */
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * Returns true if this DAG contains all nodes in the specified collection.
     *
     * @param c the collection to be checked for containment
     * @return true if this DAG contains all nodes in the specified collection
     */
    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return map.keySet().containsAll(c);
    }

    /**
     * Returns an iterator over the nodes in this DAG.
     *
     * @return an iterator over the nodes in this DAG
     */
    @Override
    public @NotNull Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    /**
     * Returns an array containing all nodes in this DAG.
     *
     * @return an array containing all nodes in this DAG
     */
    @Override
    public Object [] toArray() {
        return map.keySet().toArray();
    }

    /**
     * Returns an array containing all nodes in this DAG.
     *
     * @param array the array into which the nodes are to be stored
     * @return an array containing all nodes in this DAG
     */
    @Override
    public <T> T [] toArray(T [] array) {
        return map.keySet().toArray(array);
    }

    /**
     * Adds a node to this DAG without any edges.
     *
     * @param e the node to be added
     * @return true if the node was added
     */
    @Override
    public boolean add(E e) {
        map.putIfAbsent(e, new HashSet<>());
        size = map.size();
        return true;
    }

    /**
     * Removes a node and all its edges from this DAG.
     *
     * @param o the node to be removed
     * @return true if the node was removed
     */
    @Override
    public boolean remove(Object o) {
        if (map.remove(o) != null) {
            map.values().forEach(set -> set.remove(o));
            size = map.size();
            return true;
        }
        return false;
    }

    /**
     * Adds all nodes in the specified collection to this DAG.
     *
     * @param c the collection containing nodes to be added
     * @return true if this DAG changed as a result of the call
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        c.forEach(this::add);
        return true;
    }

    /**
     * Removes all nodes in the specified collection from this DAG.
     *
     * @param c the collection containing nodes to be removed
     * @return true if this DAG changed as a result of the call
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().map(this::remove).reduce(false, (a, b) -> a || b);
    }

    /**
     * Retains only the nodes in this DAG that are contained in the specified collection.
     *
     * @param c the collection containing nodes to be retained
     * @return true if this DAG changed as a result of the call
     * @throws NullPointerException if the specified collection is null
     */
    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        Objects.requireNonNull(c, "Collection is null");
        boolean modified = map.keySet().retainAll(c);
        if (modified) {
            map.values().forEach(set -> set.retainAll(c));
            size = map.size();
        }
        return modified;
    }

    /**
     * Creates a new Node with the specified data and children.
     *
     * @param data the data for the node
     * @param children the children of the node
     * @return a new Node instance
     */
    public Node<E> newNode(E data, Set<Edge<E>> children) {
        return new Node<>(data, children);
    }

    /**
     * Returns all child edges for the specified parent node.
     *
     * @param parent the parent node
     * @return a set of Edge objects representing the child edges
     */
    public Set<Edge<E>> getChildEdges(E parent) {
        Set<E> children = map.getOrDefault(parent, Collections.emptySet());
        if (children.isEmpty()) return Collections.emptySet();
        return children.stream()
                .map(child -> newNode(child, getChildEdges(child)))
                .collect(Collectors.toSet());
    }

    /**
     * Removes all nodes and edges from this DAG.
     */
    @Override
    public void clear() {
        map.clear();
        size = 0;
    }

    /**
     * Checks if adding an edge from key to value would create a cycle.
     * Uses breadth-first search to detect potential cycles.
     *
     * @param key source node
     * @param value target node
     * @return true if adding the edge would create a cycle
     */
    public boolean wouldCreateCycle(E key, E value) {
        if (key.equals(value)) return true;
        
        Set<E> visited = new HashSet<>(8);
        Deque<E> queue = new ArrayDeque<>(8);
        queue.add(value);
        
        while (!queue.isEmpty()) {
            E current = queue.poll();
            if (current.equals(key)) return true;
            
            Set<E> edges = map.get(current);
            if (edges != null) {
                for (E edge : edges) {
                    if (visited.add(edge)) {
                        queue.add(edge);
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Puts an edge from key to value only if it doesn't create a cycle.
     * This method ensures the graph remains acyclic.
     *
     * @param key source node
     * @param value target node
     * @return true if the edge was added, false if it would create a cycle
     */
    public boolean putIfAcyclic(E key, E value) {
        if (wouldCreateCycle(key, value)) {
            return false;
        }
        return put(key, value);
    }

    /**
     * Performs a topological sort of the DAG.
     * A topological sort is a linear ordering of vertices such that for every directed edge (u,v),
     * vertex u comes before v in the ordering.
     *
     * @return a list of nodes in topological order
     * @throws IllegalStateException if the graph contains a cycle
     */
    public List<E> topologicalSort() {
        // Initialize in-degree for all nodes
        Map<E, Integer> inDegree = map.keySet()
                .stream()
                .collect(Collectors.toMap(node -> node, node -> 0, (a, b) -> b));
        
        // Calculate in-degree for each node
        map.forEach((node, edges) -> 
            edges.forEach(edge -> 
                inDegree.merge(edge, 1, Integer::sum)));
        
        // Queue nodes with in-degree of 0
        Queue<E> queue = new LinkedList<>();
        inDegree.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .forEach(queue::add);
        
        List<E> result = new ArrayList<>();
        
        // Process nodes
        while (!queue.isEmpty()) {
            E node = queue.poll();
            result.add(node);
            
            Set<E> edges = map.get(node);
            if (edges != null) {
                for (E edge : edges) {
                    inDegree.put(edge, inDegree.get(edge) - 1);
                    if (inDegree.get(edge) == 0) {
                        queue.add(edge);
                    }
                }
            }
        }
        
        // If result size is less than the number of nodes, there's a cycle
        if (result.size() != map.size()) {
            throw new IllegalStateException("Graph contains a cycle");
        }
        
        return result;
    }

    /**
     * Implementation of the Edge interface for this DAG.
     *
     * @param <E> the type of data in this node
     */
    @AllArgsConstructor
    public static class Node<E> implements Edge<E> {
        private E data;
        private Set<Edge<E>> children;

        /**
         * Returns the data stored in this node.
         *
         * @return the data stored in this node
         */
        @Override
        public E getData() {
            return data;
        }

        /**
         * Sets the data for this node.
         *
         * @param data the new data for this node
         */
        @Override
        public void setData(E data) {
            this.data = data;
        }

        /**
         * Returns the children of this node.
         *
         * @return the set of child edges
         */
        @Override
        public Set<Edge<E>> getChildren() {
            return children;
        }

        /**
         * Sets the children for this node.
         *
         * @param children the new set of child edges
         */
        @Override
        public void setChildren(Set<Edge<E>> children) {
            this.children = children;
        }
    }
}
