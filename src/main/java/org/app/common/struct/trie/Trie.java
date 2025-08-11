package org.app.common.struct.trie;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * An interface for a Trie (prefix tree) data structure.
 * <p>
 * A Trie is a tree-like data structure that is used to store a dynamic set of strings,
 * where the keys are usually strings. A Trie is an efficient information retrieval
 * data structure that can be used to search a key in O(m) time, where m is the length
 * of the key.
 * </p>
 * <p>
 * This interface defines the basic operations that can be performed on a Trie.
 * </p>
 *
 * @param <V> the type of values associated with keys in this Trie
 */
public interface Trie<V> extends Serializable {

    /**
     * Inserts a key-value pair into this Trie.
     *
     * @param key the key to insert
     * @param value the value to associate with the key
     * @return the previous value associated with the key, or null if there was no mapping
     * @throws NullPointerException if the key is null
     */
    V insert(String key, V value);

    /**
     * Searches for a key in this Trie.
     *
     * @param key the key to search for
     * @return true if the key exists in this Trie, false otherwise
     * @throws NullPointerException if the key is null
     */
    boolean search(String key);

    /**
     * Searches for a key in this Trie and returns its associated value.
     *
     * @param key the key to search for
     * @return the value associated with the key, or null if the key doesn't exist
     * @throws NullPointerException if the key is null
     */
    V get(String key);

    /**
     * Checks if there is any key in this Trie that starts with the given prefix.
     *
     * @param prefix the prefix to check
     * @return true if there is any key with the given prefix, false otherwise
     * @throws NullPointerException if the prefix is null
     */
    boolean startsWith(String prefix);

    /**
     * Removes a key from this Trie.
     *
     * @param key the key to remove
     * @return the value associated with the key, or null if the key doesn't exist
     * @throws NullPointerException if the key is null
     */
    V remove(String key);

    /**
     * Returns all keys in this Trie.
     *
     * @return a list of all keys in this Trie
     */
    List<String> keys();

    /**
     * Returns all keys in this Trie that start with the given prefix.
     *
     * @param prefix the prefix to filter keys
     * @return a list of keys that start with the given prefix
     * @throws NullPointerException if the prefix is null
     */
    List<String> keysWithPrefix(String prefix);

    /**
     * Returns the number of keys in this Trie.
     *
     * @return the number of keys
     */
    int size();

    /**
     * Returns true if this Trie contains no keys.
     *
     * @return true if this Trie contains no keys
     */
    boolean isEmpty();

    /**
     * Removes all keys from this Trie.
     */
    void clear();
}