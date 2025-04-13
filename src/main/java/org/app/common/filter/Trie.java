package org.app.common.filter;

import java.util.*;

// Used for: Spell checking, search suggestions, autocomplete, fuzzy search
public class Trie {
    static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEndOfWord = false;
    }

    private final TrieNode root = new TrieNode();

    // Insert a word into the trie
    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toLowerCase().toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfWord = true;
    }

    // Spell check: return true if word exists
    public boolean exists(String word) {
        TrieNode node = root;
        for (char c : word.toLowerCase().toCharArray()) {
            node = node.children.get(c);
            if (node == null) return false;
        }
        return node.isEndOfWord;
    }

    // Autocomplete: return all suggestions with the given prefix
    public List<String> autocomplete(String prefix) {
        List<String> results = new ArrayList<>();
        TrieNode node = root;

        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.children.get(c);
            if (node == null) return results; // No suggestions
        }

        dfs(prefix, node, results);
        return results;
    }

    private void dfs(String prefix, TrieNode node, List<String> results) {
        if (node.isEndOfWord) results.add(prefix);

        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            dfs(prefix + entry.getKey(), entry.getValue(), results);
        }
    }

    // Levenshtein (edit-distance) fuzzy search (typo-tolerant search)
    public List<String> fuzzySearch(String input, int maxDistance) {
        int len = input.length();
        List<String> results = new ArrayList<>();
        int[] currentRow = new int[len + 1];

        for (int i = 0; i <= len; i++) {
            currentRow[i] = i;
        }

        for (Map.Entry<Character, TrieNode> entry : root.children.entrySet()) {
            dfs(entry.getValue(), String.valueOf(entry.getKey()), input, currentRow, results, maxDistance);
        }

        return results;
    }

    private void dfs(TrieNode node, String wordSoFar, String input, int[] prevRow, List<String> results, int maxDistance) {
        int len = input.length();
        int[] currentRow = new int[len + 1];
        currentRow[0] = prevRow[0] + 1;

        for (int i = 1; i <= len; i++) {
            int insertCost = currentRow[i - 1] + 1;
            int deleteCost = prevRow[i] + 1;
            int replaceCost = prevRow[i - 1] + ((int) input.charAt(i - 1) == (int) wordSoFar.charAt(wordSoFar.length() - 1) ? 0 : 1);
            currentRow[i] = Math.min(Math.min(insertCost, deleteCost), replaceCost);
        }

        if (node.isEndOfWord && currentRow[input.length()] <= maxDistance) {
            results.add(wordSoFar);
        }

        if (Arrays.stream(currentRow).min().getAsInt() <= maxDistance) {
            for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
                dfs(entry.getValue(), wordSoFar + entry.getKey(), input, currentRow, results, maxDistance);
            }
        }
    }
}