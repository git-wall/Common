package org.app.common.struct.trie;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * A hash-based implementation of a Trie (prefix tree) data structure.
 * <p>
 * This implementation uses a HashMap at each node to store children,
 * providing efficient operations for insertion, search, and prefix matching.
 * </p>
 * <p>
 * A Trie is particularly useful for applications like autocomplete, spell checking,
 * and IP routing where efficient prefix matching is required.
 * </p>
 *
 * @param <V> the type of values associated with keys in this Trie
 */
public class HashTrie<V> implements Trie<V> {

    private static final long serialVersionUID = 1L;

    private final TrieNode<V> root;
    private int size;

    /**
     * Constructs an empty HashTrie.
     */
    public HashTrie() {
        this.root = new TrieNode<>();
        this.size = 0;
    }

    /**
     * Inserts a key-value pair into this Trie.
     *
     * @param key the key to insert
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or null if there was no mapping
     * @throws NullPointerException if the key is null
     */
    @Override
    public V insert(String key, V value) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        TrieNode<V> current = root;
        for (char c : key.toCharArray()) {
            current = current.children.computeIfAbsent(c, k -> new TrieNode<>());
        }
        
        V oldValue = current.value;
        current.value = value;
        current.isEndOfWord = true;
        
        if (oldValue == null) {
            size++;
        }
        
        return oldValue;
    }

    /**
     * Searches for a key in this Trie.
     *
     * @param key the key to search for
     * @return true if the key exists in this Trie, false otherwise
     * @throws NullPointerException if the key is null
     */
    @Override
    public boolean search(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        TrieNode<V> node = findNode(key);
        return node != null && node.isEndOfWord;
    }

    /**
     * Searches for a key in this Trie and returns its associated value.
     *
     * @param key the key to search for
     * @return the value associated with the key, or null if the key doesn't exist
     * @throws NullPointerException if the key is null
     */
    @Override
    public V get(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        TrieNode<V> node = findNode(key);
        return (node != null && node.isEndOfWord) ? node.value : null;
    }

    /**
     * Checks if there is any key in this Trie that starts with the given prefix.
     *
     * @param prefix the prefix to check
     * @return true if there is any key with the given prefix, false otherwise
     * @throws NullPointerException if the prefix is null
     */
    @Override
    public boolean startsWith(String prefix) {
        Objects.requireNonNull(prefix, "Prefix cannot be null");
        
        return findNode(prefix) != null;
    }

    /**
     * Removes a key from this Trie.
     *
     * @param key the key to remove
     * @return the value associated with the key, or null if the key doesn't exist
     * @throws NullPointerException if the key is null
     */
    @Override
    public V remove(String key) {
        Objects.requireNonNull(key, "Key cannot be null");
        
        // If the key doesn't exist, return null
        if (!search(key)) {
            return null;
        }
        
        V value = get(key);
        removeRecursive(root, key, 0);
        size--;
        
        return value;
    }

    /**
     * Helper method for recursive removal of a key.
     *
     * @param current the current node
     * @param key the key to remove
     * @param depth the current depth in the trie
     * @return true if the node should be deleted
     */
    private boolean removeRecursive(TrieNode<V> current, String key, int depth) {
        // Base case: end of key
        if (depth == key.length()) {
            // Mark as not end of word
            current.isEndOfWord = false;
            current.value = null;
            
            // Return true if this node has no children
            return current.children.isEmpty();
        }
        
        char c = key.charAt(depth);
        TrieNode<V> child = current.children.get(c);
        
        // If child doesn't exist, key is not in trie
        if (child == null) {
            return false;
        }
        
        // If child should be deleted
        boolean shouldDeleteChild = removeRecursive(child, key, depth + 1);
        
        if (shouldDeleteChild) {
            current.children.remove(c);
            // Return true if this node is not end of word and has no other children
            return !current.isEndOfWord && current.children.isEmpty();
        }
        
        return false;
    }

    /**
     * Returns all keys in this Trie.
     *
     * @return a list of all keys in this Trie
     */
    @Override
    public List<String> keys() {
        return keysWithPrefix("");
    }

    /**
     * Returns all keys in this Trie that start with the given prefix.
     *
     * @param prefix the prefix to filter keys
     * @return a list of keys that start with the given prefix
     * @throws NullPointerException if the prefix is null
     */
    @Override
    public List<String> keysWithPrefix(String prefix) {
        Objects.requireNonNull(prefix, "Prefix cannot be null");
        
        List<String> result = new ArrayList<>();
        TrieNode<V> startNode = findNode(prefix);
        
        if (startNode != null) {
            collectKeys(startNode, new StringBuilder(prefix), result);
        }
        
        return result;
    }

    /**
     * Helper method to collect all keys starting from a node.
     *
     * @param node the starting node
     * @param prefix the current prefix
     * @param result the list to collect keys
     */
    private void collectKeys(TrieNode<V> node, StringBuilder prefix, List<String> result) {
        if (node.isEndOfWord) {
            result.add(prefix.toString());
        }
        
        for (Map.Entry<Character, TrieNode<V>> entry : node.children.entrySet()) {
            prefix.append(entry.getKey());
            collectKeys(entry.getValue(), prefix, result);
            prefix.deleteCharAt(prefix.length() - 1);
        }
    }

    /**
     * Returns the number of keys in this Trie.
     *
     * @return the number of keys
     */
    @Override
    public int size() {
        return size;
    }

    /**
     * Returns true if this Trie contains no keys.
     *
     * @return true if this Trie contains no keys
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all keys from this Trie.
     */
    @Override
    public void clear() {
        root.children.clear();
        size = 0;
    }

    /**
     * Helper method to find a node corresponding to a key.
     *
     * @param key the key to find
     * @return the node corresponding to the key, or null if not found
     */
    private TrieNode<V> findNode(String key) {
        TrieNode<V> current = root;
        
        for (char c : key.toCharArray()) {
            TrieNode<V> node = current.children.get(c);
            if (node == null) {
                return null;
            }
            current = node;
        }
        
        return current;
    }

    /**
     * Node class for the Trie.
     *
     * @param <V> the type of values stored in the node
     */
    @NoArgsConstructor
    private static class TrieNode<V> {
        private final Map<Character, TrieNode<V>> children = new HashMap<>();
        private V value;
        private boolean isEndOfWord;
    }
}