### Accessors 
- fluent : id -> gen method id()
- chain: id → gen method setId() but return class
- prefix: sID -> gen method setID() 
```java
@Accessors(fluent = true, chain = true, prefix = {"s", "bd"})
```

```java
 @Locked.Write
@Locked.Read
```