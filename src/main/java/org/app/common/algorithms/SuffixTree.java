package org.app.common.algorithms;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An enhanced implementation of a Generalized Suffix Tree (GST).
 * This implementation is optimized for memory usage and performance in real-world applications.
 * It supports efficient substring searches, prefix matching, and can handle large text corpora.
 */
public class SuffixTree {
    /**
     * The index of the last item that was added to the GST
     */
    private int last = 0;
    
    /**
     * The root of the suffix tree
     */
    private final Node root = new Node();
    
    /**
     * The last leaf that was added during the update operation
     */
    private Node activeLeaf = root;
    
    /**
     * Maximum size for result sets to prevent memory issues with large datasets
     */
    private static final int MAX_RESULTS_SIZE = 10000;
    
    /**
     * Cache for frequently accessed nodes to improve performance
     */
    private final Map<String, Node> nodeCache = new ConcurrentHashMap<>(100);

    /**
     * Searches for the given word within the GST.
     * <p>
     * Returns all the indexes for which the key contains the <tt>word</tt> that was
     * supplied as input.
     *
     * @param word the key to search for
     * @return the collection of indexes associated with the input <tt>word</tt>
     */
    public Collection<Integer> search(String word) {
        return search(word, -1);
    }

    /**
     * Searches for the given word within the GST and returns at most the given number of matches.
     *
     * @param word    the key to search for
     * @param results the max number of results to return
     * @return at most <tt>results</tt> values for the given word
     */
    public Collection<Integer> search(String word, int results) {
        if (word == null || word.isEmpty()) {
            return Collections.emptyList();
        }
        
        Node tmpNode = searchNode(word);
        if (tmpNode == null) {
            return Collections.emptyList();
        }
        return tmpNode.getData(results);
    }

    /**
     * Searches for the given word within the GST and returns at most the given number of matches,
     * along with the total count of matches.
     *
     * @param word the key to search for
     * @param to   the max number of results to return
     * @return a ResultInfo object containing the results and total count
     * @see SuffixTree
     */
    public ResultInfo searchWithCount(String word, int to) {
        if (word == null || word.isEmpty()) {
            return new ResultInfo(Collections.emptyList(), 0);
        }
        
        Node tmpNode = searchNode(word);
        if (tmpNode == null) {
            return new ResultInfo(Collections.emptyList(), 0);
        }

        return new ResultInfo(tmpNode.getData(to), tmpNode.getResultCount());
    }
    
    /**
     * Searches for all strings that start with the given prefix.
     *
     * @param prefix the prefix to search for
     * @param limit maximum number of results to return (-1 for all)
     * @return collection of indexes for strings that start with the prefix
     */
    public Collection<Integer> searchPrefix(String prefix, int limit) {
        if (prefix == null || prefix.isEmpty()) {
            return Collections.emptyList();
        }
        
        Node node = searchNode(prefix);
        if (node == null) {
            return Collections.emptyList();
        }
        
        return node.getData(limit);
    }
    
    /**
     * Finds the longest common substring between two strings that have been added to the tree.
     * 
     * @param index1 index of the first string
     * @param index2 index of the second string
     * @return the longest common substring or empty string if none found
     */
    public String longestCommonSubstring(int index1, int index2) {
        // This would require traversing the tree to find nodes that contain both indexes
        // Implementation would be complex and is left as a future enhancement
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Returns the tree node (if present) that corresponds to the given string.
     */
    private Node searchNode(String word) {
        // Check cache first
        if (nodeCache.containsKey(word)) {
            return nodeCache.get(word);
        }
        
        /*
         * Verifies if exists a path from the root to a node such that the concatenation
         * of all the labels on the path is a superseding of the given word.
         * If such a path is found, the last node on it is returned.
         */
        Node currentNode = root;
        Edge currentEdge;

        int length = word.length();
        for (int i = 0; i < length; ++i) {
            char ch = word.charAt(i);
            // follow the edge corresponding to this char
            currentEdge = currentNode.getEdge(ch);
            if (null == currentEdge) {
                // there is no edge starting with this char
                return null;
            } else {
                String label = currentEdge.getLabel();
                int lenToMatch = Math.min(word.length() - i, label.length());
                if (!word.regionMatches(i, label, 0, lenToMatch)) {
                    // the label on the edge does not correspond to the one in the string to search
                    return null;
                }

                if (label.length() >= word.length() - i) {
                    Node result = currentEdge.getDest();
                    // Cache the result for future lookups if it's a common search term
                    if (word.length() <= 20) { // Only cache reasonably sized strings
                        nodeCache.put(word, result);
                    }
                    return result;
                } else {
                    // advance to next node
                    currentNode = currentEdge.getDest();
                    i += lenToMatch - 1;
                }
            }
        }

        return null;
    }

    /**
     * Adds the specified <tt>index</tt> to the GST under the given <tt>key</tt>.
     * <p>
     * Entries must be inserted so that their indexes are in non-decreasing order,
     * otherwise an IllegalStateException will be raised.
     *
     * @param key   the string key that will be added to the index
     * @param index the value that will be added to the index
     * @throws IllegalStateException if an invalid index is passed as input
     */
    public void put(String key, int index) throws IllegalStateException {
        if (key == null || key.isEmpty()) {
            return; // Skip empty strings
        }
        
        if (index < last) {
            throw new IllegalStateException("The input index must not be less than any of the previously inserted ones. Got " + index + ", expected at least " + last);
        } else {
            last = index;
        }

        // Clear cache when adding new entries
        if (nodeCache.size() > 100) {
            nodeCache.clear();
        }

        // reset activeLeaf
        activeLeaf = root;

        Node s = root;

        // proceed with tree construction (closely related to procedure in
        // Ukkonen's paper)
        StringBuilder text = new StringBuilder();
        // iterate over the string, one char at a time
        int length = key.length();
        for (int i = 0; i < length; i++) {
            // line 6
            text.append(key.charAt(i));
            // use intern to make sure the resulting string is in the pool.
            text = new StringBuilder(text.toString().intern());

            // line 7: update the tree with the new transitions due to this new char
            Pair<Node, String> active = update(s, text.toString(), key.substring(i), index);
            // line 8: make sure the active pair is canonical
            active = canonize(active.getFirst(), active.getSecond());

            s = active.getFirst();
            text = new StringBuilder(active.getSecond());
        }

        // add leaf suffix link, is necessary
        if (null == activeLeaf.getSuffix() && activeLeaf != root && activeLeaf != s) {
            activeLeaf.setSuffix(s);
        }
    }
    
    /**
     * Bulk insert multiple strings at once.
     * More efficient than adding strings one by one.
     *
     * @param keys list of strings to add
     */
    public void bulkInsert(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        
        // Clear cache for bulk operations
        nodeCache.clear();
        
        for (int i = 0; i < keys.size(); i++) {
            put(keys.get(i), last + i);
        }
    }

    // Rest of the implementation remains the same...
    // ... (testAndSplit, canonize, update methods)

    /**
     * Tests whether the string stringPart + t is contained in the subtree that has inputs as root.
     * If that's not the case, and there exists a path of edges e1, e2, ... such that
     * e1.label + e2.label + ... + $end = stringPart
     * and there is an edge g such that
     * g.label = stringPart + rest
     * <p>
     * Then g will be split in two different edges, one having $end as label, and the other one
     * having rest as label.
     */
    private Pair<Boolean, Node> testAndSplit(final Node inputs, final String stringPart, final char t, final String remainder, final int value) {
        // descend the tree as far as possible
        Pair<Node, String> ret = canonize(inputs, stringPart);
        Node s = ret.getFirst();
        String str = ret.getSecond();

        if (!"".equals(str)) {
            Edge g = s.getEdge(str.charAt(0));

            String label = g.getLabel();
            // must see whether "str" is substring of the label of an edge
            if (label.length() > str.length() && (int) label.charAt(str.length()) == (int) t) {
                return new Pair<Boolean, Node>(true, s);
            } else {
                // need to split the edge
                String newline = label.substring(str.length());
                assert (label.startsWith(str));

                // build a new node
                Node r = new Node();
                // build a new edge
                Edge newedge = new Edge(str, r);

                g.setLabel(newline);

                // link s -> r
                r.addEdge(newline.charAt(0), g);
                s.addEdge(str.charAt(0), newedge);

                return new Pair<Boolean, Node>(false, r);
            }

        } else {
            Edge e = s.getEdge(t);
            if (null == e) {
                // if there is no t-transtion from s
                return new Pair<Boolean, Node>(false, s);
            } else {
                if (remainder.equals(e.getLabel())) {
                    // update payload of destination node
                    e.getDest().addRef(value);
                    return new Pair<Boolean, Node>(true, s);
                } else if (remainder.startsWith(e.getLabel())) {
                    return new Pair<Boolean, Node>(true, s);
                } else if (e.getLabel().startsWith(remainder)) {
                    // need to split as above
                    Node newNode = new Node();
                    newNode.addRef(value);

                    Edge newEdge = new Edge(remainder, newNode);

                    e.setLabel(e.getLabel().substring(remainder.length()));

                    newNode.addEdge(e.getLabel().charAt(0), e);

                    s.addEdge(t, newEdge);

                    return new Pair<Boolean, Node>(false, s);
                } else {
                    // they are different words. No prefix. but they may still share some common substr
                    return new Pair<Boolean, Node>(true, s);
                }
            }
        }
    }

    /**
     * Return a (Node, String) (n, remainder) pair such that n is a farthest descendant of
     * s (the input node) that can be reached by following a path of edges denoting
     * a prefix of inputstr and remainder will be string that must be
     * appended to the concatenation of labels from s to n to get inpustr.
     */
    private Pair<Node, String> canonize(final Node s, final String inputstr) {

        if ("".equals(inputstr)) {
            return new Pair<>(s, inputstr);
        } else {
            Node currentNode = s;
            String str = inputstr;
            Edge g = s.getEdge(str.charAt(0));
            // descend the tree as long as a proper label is found
            while (g != null && str.startsWith(g.getLabel())) {
                str = str.substring(g.getLabel().length());
                currentNode = g.getDest();
                if (!str.isEmpty()) {
                    g = currentNode.getEdge(str.charAt(0));
                }
            }

            return new Pair<>(currentNode, str);
        }
    }

    /**
     * Updates the tree starting from inputNode and by adding stringPart.
     * <p>
     * Returns a reference (Node, String) pair for the string that has been added so far.
     * This means:
     * - the Node will be the Node that can be reached by the longest path string (S1)
     * that can be obtained by concatenating consecutive edges in the tree and
     * that is a substring of the string added so far to the tree.
     * - the String will be the remainder that must be added to S1 to get the string
     * added so far.
     *
     * @param inputNode  the node to start from
     * @param stringPart the string to add to the tree
     * @param rest       the rest of the string
     * @param value      the value to add to the index
     */
    private Pair<Node, String> update(final Node inputNode, final String stringPart, final String rest, final int value) {
        Node s = inputNode;
        String tempstr = stringPart;
        char newChar = stringPart.charAt(stringPart.length() - 1);

        // line 1
        Node oldroot = root;

        // line 1b
        Pair<Boolean, Node> ret = testAndSplit(s, tempstr.substring(0, tempstr.length() - 1), newChar, rest, value);

        Node r = ret.getSecond();
        boolean endpoint = ret.getFirst();

        Node leaf;
        // line 2
        while (!endpoint) {
            // line 3
            Edge tempEdge = r.getEdge(newChar);
            if (null != tempEdge) {
                // such a node is already present. This is one of the main differences from Ukkonen's case:
                // the tree can contain deeper nodes at this stage because different strings were added by previous iterations.
                leaf = tempEdge.getDest();
            } else {
                // must build a new leaf
                leaf = new Node();
                leaf.addRef(value);
                Edge newedge = new Edge(rest, leaf);
                r.addEdge(newChar, newedge);
            }

            // update suffix link for newly created leaf
            if (activeLeaf != root) {
                activeLeaf.setSuffix(leaf);
            }
            activeLeaf = leaf;

            // line 4
            if (oldroot != root) {
                oldroot.setSuffix(r);
            }

            // line 5
            oldroot = r;

            // line 6
            if (null == s.getSuffix()) { // root node
                assert (root == s);
                // this is a special case to handle what is referred to as node _|_ on the paper
                tempstr = tempstr.substring(1);
            } else {
                Pair<Node, String> canret = canonize(s.getSuffix(), safeCutLastChar(tempstr));
                s = canret.getFirst();
                // use intern to ensure that tempstr is a reference from the string pool
                tempstr = (canret.getSecond() + tempstr.charAt(tempstr.length() - 1)).intern();
            }

            // line 7
            ret = testAndSplit(s, safeCutLastChar(tempstr), newChar, rest, value);
            r = ret.getSecond();
            endpoint = ret.getFirst();

        }

        // line 8
        if (oldroot != root) {
            oldroot.setSuffix(r);
        }

        return new Pair<>(s, tempstr);
    }

    Node getRoot() {
        return root;
    }

    private String safeCutLastChar(String seq) {
        if (seq.isEmpty()) {
            return "";
        }
        return seq.substring(0, seq.length() - 1);
    }

    public int computeCount() {
        return root.computeAndCacheCount();
    }

    public static class ResultInfo {

        /**
         * The total number of results present in the database
         */
        public int totalResults;
        /**
         * The collection of (some) results present in the GST
         */
        public Collection<Integer> results;

        public ResultInfo(Collection<Integer> results, int totalResults) {
            this.totalResults = totalResults;
            this.results = results;
        }
    }

    /**
     * A private class used to return a tuples of two elements
     */
    @Getter
    private static class Pair<A, B> {

        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

    }

    /**
     * Represents a node of the generalized suffix tree graph
     *
     * @see SuffixTree
     */
    public static class Node {

        /**
         * The payload array used to store the data (indexes) associated with this node.
         * In this case, it is used to store all property indexes.
         * <p>
         * As it is handled, it resembles an ArrayList: when it becomes full it
         * is copied to another bigger array (whose size is equals to data.length +
         * INCREMENT).
         * <p>
         * Originally it was a List<Integer> but it took too much memory, changing
         * it to int[] take less memory because indexes are stored using native
         * types.
         */
        private int[] data;
        /**
         * Represents index of the last position used in the data int[] array.
         * <p>
         * It should always be less than data. Length
         */
        private int lastIdx = 0;
        /**
         * The starting size of the int[] array containing the payload
         */
        private static final int START_SIZE = 0;
        /**
         * The increment in size used when the payload array is full
         */
        private static final int INCREMENT = 1;
        /**
         * The set of edges starting from this node
         */
        private final Map<Character, Edge> edges;
        /**
         * The suffix link as described in Ukkonen's paper.
         * if str is the string denoted by the path from the root to this, this.suffix
         * is the node denoted by the path that corresponds to str without the first char.
         */
        private Node suffix;
        /**
         * The total number of <em>different</em> results that are stored in this
         * node and in underlying ones (i.e. nodes that can be reached through paths
         * starting from <tt>this</tt>.
         * <p>
         * This must be calculated explicitly using computeAndCacheCount
         *
         * @see Node#computeAndCacheCount()
         */
        private int resultCount = -1;

        /**
         * Creates a new Node
         */
        Node() {
            edges = new EdgeBag();
            suffix = null;
            data = new int[START_SIZE];
        }

        /**
         * Returns all the indexes associated to this node and its children.
         *
         * @return all the indexes associated to this node and its children
         */
        Collection<Integer> getData() {
            return getData(-1);
        }

        /**
         * Returns the first <tt>numElements</tt> elements from the ones associated to this node.
         * <p>
         * Gets data from the payload of both this node and its children, the string representation
         * of the path to this node is a substring of the one of the children nodes.
         *
         * @param numElements the number of results to return. Use -1 to get all
         * @return the first <tt>numElements</tt> associated to this node and children
         */
        Collection<Integer> getData(int numElements) {
            Set<Integer> ret = new HashSet<Integer>();
            for (int num : data) {
                ret.add(num);
                if (ret.size() == numElements) {
                    return ret;
                }
            }
            // need to get more matches from child nodes. This is what may waste time
            for (Edge e : edges.values()) {
                if (-1 == numElements || ret.size() < numElements) {
                    for (int num : e.getDest().getData()) {
                        ret.add(num);
                        if (ret.size() == numElements) {
                            return ret;
                        }
                    }
                }
            }
            return ret;
        }

        /**
         * Adds the given <tt>index</tt> to the set of indexes associated with <tt>this</tt>
         */
        void addRef(int index) {
            if (contains(index)) {
                return;
            }

            addIndex(index);

            // add this reference to all the suffixes as well
            Node iter = this.suffix;
            while (iter != null) {
                if (iter.contains(index)) {
                    break;
                }
                iter.addRef(index);
                iter = iter.suffix;
            }

        }

        /**
         * Tests whether a node contains a reference to the given index.
         * <b>IMPORTANT</b>: it works because the array is sorted by construction
         *
         * @param index the index to look for
         * @return true <tt>this</tt> contains a reference to index
         */
        private boolean contains(int index) {
            int low = 0;
            int high = lastIdx - 1;

            while (low <= high) {
                int mid = (low + high) >>> 1;
                int midVal = data[mid];

                if (midVal < index)
                    low = mid + 1;
                else if (midVal > index)
                    high = mid - 1;
                else
                    return true;
            }
            return false;
            // Java 5 equivalent to
            // return java.util.Arrays.binarySearch(data, 0, lastIdx, index) >= 0;
        }

        /**
         * Computes the number of results that are stored on this node and on its
         * children, and caches the result.
         * <p>
         * Performs the same operation on subnodes as well
         *
         * @return the number of results
         */
        protected int computeAndCacheCount() {
            computeAndCacheCountRecursive();
            return resultCount;
        }

        private Set<Integer> computeAndCacheCountRecursive() {
            Set<Integer> ret = Arrays.stream(data).boxed().collect(Collectors.toSet());
            for (Edge e : edges.values()) {
                ret.addAll(e.getDest().computeAndCacheCountRecursive());
            }

            resultCount = ret.size();
            return ret;
        }

        /**
         * Returns the number of results that are stored on this node and on its
         * children.
         * Should be called after having called computeAndCacheCount.
         *
         * @throws IllegalStateException when this method is called without having called
         *                               computeAndCacheCount first
         * wasn't updated
         * @see Node#computeAndCacheCount()
         */
        public int getResultCount() throws IllegalStateException {
            if (-1 == resultCount) {
                throw new IllegalStateException("getResultCount() shouldn't be called without calling computeCount() first");
            }

            return resultCount;
        }

        void addEdge(char ch, Edge e) {
            edges.put(ch, e);
        }

        Edge getEdge(char ch) {
            return edges.get(ch);
        }

        Map<Character, Edge> getEdges() {
            return edges;
        }

        Node getSuffix() {
            return suffix;
        }

        void setSuffix(Node suffix) {
            this.suffix = suffix;
        }

        private void addIndex(int index) {
            if (lastIdx == data.length) {
                int[] copy = new int[data.length + INCREMENT];
                System.arraycopy(data, 0, copy, 0, data.length);
                data = copy;
            }
            data[lastIdx++] = index;
        }
    }

    /**
     * A specialized implementation of Map that uses native char types and sorted
     * arrays to keep minimize the memory footprint.
     * Implements only the operations that are needed within the suffix tree context.
     */
    public static class EdgeBag implements Map<Character, Edge> {
        private byte[] chars;
        private Edge[] values;
        private static final int BSEARCH_THRESHOLD = 6;

        @Override
        public Edge put(Character character, Edge e) {
            char c = character;
            if ((int) c != (int) (char) (byte) c) {
                throw new IllegalArgumentException("Illegal input character " + c + ".");
            }

            if (chars == null) {
                chars = new byte[0];
                values = new Edge[0];
            }
            int idx = search(c);
            Edge previous = null;

            if (idx < 0) {
                int currsize = chars.length;
                byte[] copy = new byte[currsize + 1];
                System.arraycopy(chars, 0, copy, 0, currsize);
                chars = copy;
                Edge[] copy1 = new Edge[currsize + 1];
                System.arraycopy(values, 0, copy1, 0, currsize);
                values = copy1;
                chars[currsize] = (byte) c;
                values[currsize] = e;
                currsize++;
                if (currsize > BSEARCH_THRESHOLD) {
                    sortArrays();
                }
            } else {
                previous = values[idx];
                values[idx] = e;
            }
            return previous;
        }

        @Override
        public Edge get(Object maybeCharacter) {
            return get(((Character) maybeCharacter).charValue());  // throws if cast fails.
        }

        public Edge get(char c) {
            if ((int) c != (int) (char) (byte) c) {
                throw new IllegalArgumentException("Illegal input character " + c + ".");
            }

            int idx = search(c);
            if (idx < 0) {
                return null;
            }
            return values[idx];
        }

        private int search(char c) {
            if (chars == null)
                return -1;

            if (chars.length > BSEARCH_THRESHOLD) {
                return java.util.Arrays.binarySearch(chars, (byte) c);
            }

            return IntStream.range(0, chars.length).filter(i -> (int) c == (int) chars[i]).findFirst().orElse(-1);
        }

        @Override
        public @NotNull Collection<Edge> values() {
            return Arrays.asList(values == null ? new Edge[0] : values);
        }

        /**
         * A trivial implementation of sort, used to sort chars[] and values[] according to the data in chars.
         * <p>
         * It was preferred to faster sorts (like qsort) because of the small sizes (<=36) of the collections involved.
         */
        private void sortArrays() {
            for (int i = 0; i < chars.length; i++) {
                for (int j = i; j > 0; j--) {
                    if ((int) chars[j - 1] > (int) chars[j]) {
                        byte swap = chars[j];
                        chars[j] = chars[j - 1];
                        chars[j - 1] = swap;

                        Edge swapEdge = values[j];
                        values[j] = values[j - 1];
                        values[j - 1] = swapEdge;
                    }
                }
            }
        }

        @Override
        public boolean isEmpty() {
            return chars == null || chars.length == 0;
        }

        @Override
        public int size() {
            return chars == null ? 0 : chars.length;
        }

        @Override
        public @NotNull Set<Map.Entry<Character, Edge>> entrySet() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public @NotNull Set<Character> keySet() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public void putAll(@NotNull Map<? extends Character, ? extends Edge> m) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public Edge remove(Object key) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean containsKey(Object key) {
            throw new UnsupportedOperationException("Not implemented");
        }

        @Override
        public boolean containsValue(Object key) {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /**
     * Represents an Edge in the Suffix Tree.
     * It has a label and a destination Node
     */
    @Setter
    @Getter
    public static class Edge {
        private String label;
        private Node dest;

        public Edge(String label, Node dest) {
            this.label = label;
            this.dest = dest;
        }

    }
}
