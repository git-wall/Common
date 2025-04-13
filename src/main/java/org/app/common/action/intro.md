## 🔤 Trie (Prefix Tree)
🧠 What it solves:
Fast prefix-based search (autocomplete, dictionary)

Space-efficient for storing sets of strings with common prefixes

🛠️ Real-world use:
Search engines (Google suggest/autocomplete)

Dictionary APIs (spell checkers, lexicons)

IP routing tables

Word games (e.g., Scrabble, Boggle AI)

## 🌸 Bloom Filter
🧠 What it solves:
Probabilistic membership check (is X in the set?) with space efficiency

Extremely fast and low memory footprint, at the cost of false positives

🛠️ Real-world use:
Databases like Cassandra, HBase, Bigtable

Preventing duplicate work in crawlers (Googlebot)

Caching layers (check before expensive DB call)

Git: to avoid downloading unchanged files

## 📦 Counting Bloom Filter
Like Bloom, but allows counting items (can support deletion)

Useful for distributed systems tracking occurrences

## 🔁 Cuckoo Filter
Alternative to Bloom Filter that supports deletion natively

Better performance in some cases

Used in network systems, like load balancers and routers

## 📚 Suffix Tree / Suffix Array
Used for full-text search, substring matching (not just prefix like Trie)

Super useful in:

Bioinformatics (DNA sequence search)

Text editors (find/replacement features)

Compression (e.g., BWT in bzip2)

## 📥 LRU Cache (Least Recently Used)
Keeps track of recently-used items

Used in:

Redis LRU mode

Web browser history/cache

Memory management in OS

## 🧮 HyperLogLog
Probabilistically estimates number of distinct elements in a stream

Very low memory usage (~12 KB)

Used in:

Redis (PFADD / PFCOUNT)

Analytics systems (unique visitors count, etc.)

## 🗂️ B+ Tree
Balanced tree used in most databases (PostgreSQL, MySQL)

Optimized for disk access

## 🔃 Skip List
Alternative to balanced binary trees

Used in Redis as part of Sorted Sets

Good for range queries with ordered keys

## 🗺️ Hash Trie (or HAMT - Hash Array Mapped Trie)
Used in functional languages like Clojure, Scala

Persistent (immutable) map structure


| Data Structure   | Type                  | Strength                        | Used In                          |
|-------------------|-----------------------|----------------------------------|-----------------------------------|
| Trie             | Tree                 | Fast prefix search              | Search engines, dictionaries     |
| Bloom Filter     | Probabilistic Set    | Fast membership test            | Caching, databases, CDNs         |
| Cuckoo Filter    | Probabilistic Set    | Membership + deletion support   | Networking, caching              |
| HyperLogLog      | Probabilistic Count  | Cardinality estimation          | Analytics, Redis                 |
| LRU Cache        | Cache                | Memory-aware eviction           | Browsers, Redis                  |
| B+ Tree          | Tree                 | Disk-based lookup               | Databases                        |
| Suffix Tree      | Tree                 | Substring search                | Text indexing, bioinformatics    |
| Skip List        | List (layered)       | Ordered structure w/ fast inserts | Redis                          |