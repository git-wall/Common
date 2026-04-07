### CompactSet 
- better than hashset about memory
```java
CompactSet<Object> sss = new CompactSet<>(List.of("a","b"));
```

### HashDag 
- DAG with hash

```java
Dag<Object> dag = new HashDag<>();
Deque<Object> deque = dag.getRoots();
while (!deque.isEmpty()) {
    Object o = deque.pollLast();
    Set<Object> edges = dag.getEdges(o);
    // TODO:
}
```

### ArrayUtils 
```java
List<String> b = new ArrayList<>();
List<String> a = ArrayUtils.sort(b);
```

### LevenshteinSearch
```java
List<String> b = new ArrayList<>();
List<String> data = LevenshteinSearch.search("haha", b);
```

