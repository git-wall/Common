```java
class HashTrieExample {
    public static void main(String[] args) {
        // Create a new HashTrie
        Trie<Integer> trie = new HashTrie<>();

        // Insert key-value pairs
        trie.insert("apple", 1);
        trie.insert("application", 2);
        trie.insert("banana", 3);
        trie.insert("ball", 4);
        trie.insert("cat", 5);

        System.out.println("Trie size: " + trie.size());  // Output: 5

        // Search for keys
        System.out.println("\nSearch operations:");
        System.out.println("Contains 'apple': " + trie.search("apple"));       // true
        System.out.println("Contains 'app': " + trie.search("app"));           // false
        System.out.println("Contains 'banana': " + trie.search("banana"));     // true
        System.out.println("Contains 'orange': " + trie.search("orange"));     // false

        // Get values associated with keys
        System.out.println("\nGet operations:");
        System.out.println("Value for 'apple': " + trie.get("apple"));         // 1
        System.out.println("Value for 'application': " + trie.get("application")); // 2
        System.out.println("Value for 'orange': " + trie.get("orange"));       // null

        // Check if trie contains keys with a given prefix
        System.out.println("\nPrefix operations:");
        System.out.println("Keys with prefix 'app': " + trie.startsWith("app"));   // true
        System.out.println("Keys with prefix 'ban': " + trie.startsWith("ban"));   // true
        System.out.println("Keys with prefix 'ora': " + trie.startsWith("ora"));   // false

        // Get all keys with a given prefix
        System.out.println("\nKeys with prefix 'app':");
        List<String> appKeys = trie.keysWithPrefix("app");
        appKeys.forEach(System.out::println);  // Output: apple, application

        System.out.println("\nKeys with prefix 'b':");
        List<String> bKeys = trie.keysWithPrefix("b");
        bKeys.forEach(System.out::println);    // Output: banana, ball

        // Get all keys in the trie
        System.out.println("\nAll keys in trie:");
        List<String> allKeys = trie.keys();
        allKeys.forEach(System.out::println);  // Output: apple, application, banana, ball, cat

        // Remove a key
        System.out.println("\nRemove operations:");
        Integer removedValue = trie.remove("apple");
        System.out.println("Removed value: " + removedValue);  // Output: 1
        System.out.println("Contains 'apple' after removal: " + trie.search("apple"));  // false
        System.out.println("Contains 'application' after removal: " + trie.search("application"));  // true
        System.out.println("Trie size after removal: " + trie.size());  // Output: 4

        // Update a value
        System.out.println("\nUpdate operations:");
        Integer oldValue = trie.insert("banana", 10);
        System.out.println("Old value for 'banana': " + oldValue);  // Output: 3
        System.out.println("New value for 'banana': " + trie.get("banana"));  // Output: 10

        // Clear the trie
        trie.clear();
        System.out.println("\nAfter clearing the trie:");
        System.out.println("Trie size: " + trie.size());  // Output: 0
        System.out.println("Is empty: " + trie.isEmpty());  // Output: true

        // Example of using HashTrie for autocomplete
        System.out.println("\nAutocomplete example:");
        Trie<Integer> autocompleteTrie = new HashTrie<>();
        String[] words = {"apple", "application", "append", "banana", "ball", "cat", "car", "card"};
        for (int i = 0; i < words.length; i++) {
            autocompleteTrie.insert(words[i], i);
        }

        String prefix = "ap";
        System.out.println("Autocomplete suggestions for '" + prefix + "':");
        List<String> suggestions = autocompleteTrie.keysWithPrefix(prefix);
        suggestions.forEach(System.out::println);  // Output: apple, application, append
    }
}
```
