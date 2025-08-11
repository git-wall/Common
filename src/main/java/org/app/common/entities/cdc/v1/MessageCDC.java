package org.app.common.entities.cdc.v1;

import lombok.Data;

/**
 * Unified class that holds both Debezium Kafka key and value.
 */
@Data
public class MessageCDC<T> {

    private Key key;
    private Value<T> value;

    @Data
    public static class Key {
        private Long id; // You can modify for composite keys
    }

    @Data
    public static class Value<T> {
        private Payload<T> payload;
    }

    @Data
    public static class Payload<T> {
        private T before;
        private T after;
        private Source source;
        private String op;     // c, u, d, r
        private Long ts_ms;
    }

    @Data
    public static class Source {
        private String version;
        private String connector;
        private String name;
        private String db;
        private String schema;
        private String table;
        private Long ts_ms;
        private String snapshot;
        private String txId;
        private Integer lsn;         // Postgres-specific
        private Long file;           // MySQL binlog file/position
        private Long pos;
        private Integer row;
        private Boolean deleted;     // Optional
    }
}