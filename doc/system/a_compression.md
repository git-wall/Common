# Strengths and Weaknesses

| **Algorithm** | **Compression Ratio** | **Compression Speed** | **Decompression Speed** | **Best For**                                    |
|---------------|-----------------------|-----------------------|-------------------------|-------------------------------------------------|
| gzip          | Moderate              | Slow                  | Moderate                | Archival, web content                           |
| Snappy        | Low                   | Very Fast             | Very Fast               | Real-time, low-CPU systems                      |
| LZ4           | Moderate              | Extremely Fast        | Extremely Fast          | High-throughput, low-latency systems            |
| zstd          | High                  | Fast                  | Fast                    | General-purpose, Parquet, Kafka, data transfers |

# Real-World Scenarios: When to Use What

## High-throughput streaming (Kafka)
- Use: zstd or LZ4
- Why: zstd gives better compression with good speed. LZ4 if latency is critical and CPU is limited. Snappy is acceptable if inherited, but usually not optimal anymore.

## Long-term storage (Parquet, S3)
- Use: zstd
- Why: Best compression ratio reduces storage cost and IO. Slight CPU trade-off is acceptable.

## Low-latency querying (DuckDB, Cassandra)
- Use: LZ4
- Why: Prioritize decompression speed for fast queries. LZ4 is the common choice in OLAP engines.

## CPU/memory constrained environments
- Use: Snappy or LZ4
- Why: Low CPU overhead is more important than compression ratio. zstd can still be used at low compression levels if needed.

## Fast network, low compression benefit (datacenter file transfer)
- Use: LZ4
- Why: Minimal compression overhead. On fast networks, speed beats smaller file sizes.

## Slow network or internet transfers
- Use: zstd
- Why: Better compression reduces transfer time despite slightly higher CPU cost.

# What to Remember
**_`~~No algorithm is best for every workload~~`_**
- zstd has become the Swiss Army knife of compression. Unless you have a good reason not to, it’s a smart pick.
- LZ4 is unbeatable when speed matters more than compression.
- Snappy is still acceptable in latency-sensitive, CPU-constrained setups but is generally being replaced.
- gzip remains for legacy systems or when maximum compatibility is required.