
### HyperLogLog
#### ClickHouse
```sql
CREATE TABLE IF NOT EXISTS monthly_visitors (
                                                month String,
                                                count UInt64
) ENGINE = MergeTree()
ORDER BY month;
```
#### Druid (through Kafka)
```json
{
  "type": "kafka",
  "dataSchema": {
    "dataSource": "monthly_visitors",
    "parser": {
      "type": "string",
      "parseSpec": {
        "format": "json",
        "timestampSpec": {"column": "timestamp", "format": "iso"},
        "dimensionsSpec": {
          "dimensions": ["type", "month"]
        }
      }
    },
    "metricsSpec": [
      {"type": "count", "name": "event_count"},
      {"type": "longSum", "name": "visitor_count", "fieldName": "count"}
    ],
    "granularitySpec": {
      "type": "uniform",
      "segmentGranularity": "month",
      "queryGranularity": "none",
      "rollup": true
    }
  },
  "tuningConfig": {"type": "kafka"},
  "ioConfig": {
    "topic": "analytics-events",
    "consumerProperties": {"bootstrap.servers": "localhost:9092"},
    "taskCount": 1,
    "replicas": 1
  }
}

```

#### Code

```java
@Service
public class AnalyticsService {

    private final HybridHyperLogLog hybridHyperLogLog;

    public AnalyticsService(HybridHyperLogLog hybridHyperLogLog) {
        this.hybridHyperLogLog = hybridHyperLogLog;
    }

    public void trackEvent(String category, String value) {
        // For daily tracking (e.g., "visitor123" on today's key)
        hybridHyperLogLog.addDailyEvent(category, value);

        // For monthly tracking (e.g., "visitor123" on this month's key)
        hybridHyperLogLog.addMonthlyEvent(category, value);
    }
}
//}
```