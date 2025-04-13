| Feature            | Trie                                | Bloom Filter                              |
|--------------------|-------------------------------------|-------------------------------------------|
| Accuracy           | 100% accurate                       | Might give false positives (no false negatives) |
| Memory Usage       | High (especially with many strings) | Very low (space-efficient)             |
| Speed              | Fast (O(length of string))          | Extremely fast (constant time, O(1))    |
| Can remove items?  | Yes                                 | Not easily (requires special handling)   |
| Can list all items?| Yes                                 | No (Bloom Filter doesn’t store actual items) |
| Use case           | Autocomplete, prefix search         | Membership test (e.g., "seen before?") |