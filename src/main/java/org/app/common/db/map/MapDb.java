package org.app.common.db.map;

import org.mapdb.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MapDb {
    private final DB db;
    private final MapDbProperties properties;
    private final Pipeline pipeline;
    private final Map<String, BTreeMap<String, String>> btreeTables = new ConcurrentHashMap<>();
    private final Map<String, HTreeMap<String, String>> htreeTables = new ConcurrentHashMap<>();

    public MapDb(MapDbProperties properties, Pipeline pipeline) {
        this.properties = properties;
        this.pipeline = pipeline;
        this.db = DBMaker
                .fileDB("mapdb.db")
                .transactionEnable()
                .make();
    }

    public void createTable(String name) {
        if (properties.getType() == MapDbProperties.MapType.B_TREE) {
            BTreeMap<String, String> table = db.treeMap(name, Serializer.STRING, Serializer.STRING).createOrOpen();
            btreeTables.put(name, table);
        } else {
            HTreeMap<String, String> table = db.hashMap(name, Serializer.STRING, Serializer.STRING).createOrOpen();
            htreeTables.put(name, table);
        }
        pipeline.createTable(name);
    }

    public void put(String table, String key, String value) {
        if (btreeTables.containsKey(table)) {
            btreeTables.get(table).put(key, value);
        } else if (htreeTables.containsKey(table)) {
            htreeTables.get(table).put(key, value);
        }

        pipeline.put(table, key, value);
    }

    public String get(String table, String key) {
        if (btreeTables.containsKey(table)) {
            return btreeTables.get(table).get(key);
        } else if (htreeTables.containsKey(table)) {
            return htreeTables.get(table).get(key);
        }
        return null;
    }

    public void delete(String table, String key) {
        if (btreeTables.containsKey(table)) {
            btreeTables.get(table).remove(key);
        } else if (htreeTables.containsKey(table)) {
            htreeTables.get(table).remove(key);
        }
        pipeline.delete(table, key);
    }

    public Set<String> keys(String table) {
        if (btreeTables.containsKey(table)) return btreeTables.get(table).getKeys();
        else if (htreeTables.containsKey(table)) return htreeTables.get(table).getKeys();
        return Set.of();
    }

    @Scheduled(fixedDelay = 60000L)
    public void backup() {
        db.commit();
    }
}
