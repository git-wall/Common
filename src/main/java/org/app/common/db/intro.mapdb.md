🗃️ Data Structures
1. HTreeMap
   A hash map implementation optimized for concurrent access and large datasets

Supports segmentation for parallel writes and auto-expanding index trees.

2. BTreeMap
   Implements a B+ tree structure, providing ordered maps with range query capabilities.

Suitable for scenarios requiring sorted data access.


3. Other Collections
   NavigableSet: Supports sorted sets with navigable methods.

Queues and Lists: Persistent implementations of standard Java queues and lists.

Bitmaps: Efficient storage and manipulation of bit sets.

🔒 Transactions and Concurrency
Transactions:

MapDB provides atomic commit and rollback functionality using a write-ahead log (WAL).


Enable transactions via transactionEnable() during DB creation.


Note: MapDB supports a single global transaction; it lacks support for multiple concurrent transactions and full ACID isolation levels. 


Concurrency:

Designed for high concurrency with fine-grained locking mechanisms.

Supports concurrent access and modifications across multiple threads. 