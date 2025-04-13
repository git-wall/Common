package org.app.common.algorithms;

import org.app.common.db.QuerySupplier;

import java.util.List;

public class Trie<T> {
    private final org.app.common.filter.Trie trie;

    private QuerySupplier<T> query;

    public Trie() {
        this.trie = new org.app.common.filter.Trie();
    }

    public Trie<T> query(QuerySupplier<T> query) {
        this.query = query;
        return this;
    }

    public Trie<T> load() {
        // Load data from DB to Trie
        query.getFindFields().get().forEach(trie::insert);
        return this;
    }

    public boolean exists(String element) {
        return trie.exists(element);
    }

    public List<String> suggestions(String prefix) {
        return trie.autocomplete(prefix);
    }

    public List<String> fuzzySearch(String input, int maxDistance) {
        return trie.fuzzySearch(input, maxDistance);
    }
}
