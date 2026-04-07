# application.yml hoặc JVM args
```dockerfile
JAVA_OPTS=
-Xms4g -Xmx4g                    # Heap size cố định
-XX:+UseG1GC                     # G1 garbage collector
-XX:MaxGCPauseMillis=200         # Target pause time
-XX:G1HeapRegionSize=16m         # Region size
-XX:InitiatingHeapOccupancyPercent=45  # Early GC
-XX:G1ReservePercent=10          # Reserve space
-XX:+ParallelRefProcEnabled      # Parallel ref processing

# GC logging
-Xlog:gc*:file=gc.log:time,uptime:filecount=10,filesize=100m
```




# Ultra-low latency (<10ms pause)
```dockerfile
JAVA_OPTS=
-Xms8g -Xmx8g
-XX:+UseZGC
-XX:+ZGenerational              # Java 21+
-XX:ZCollectionInterval=120     # GC interval (seconds)
```
