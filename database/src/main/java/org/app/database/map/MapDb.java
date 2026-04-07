package org.app.database.map;

import org.mapdb.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class MapDb {
    private final DB db;
    private final MapDbProperties properties;
    private final Map<String, BTreeMap<String, String>> btreeTables = new ConcurrentHashMap<>();
    private final Map<String, HTreeMap<String, String>> htreeTables = new ConcurrentHashMap<>();

    public MapDb(MapDbProperties properties) {
        this.properties = properties;
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
    }

    public void put(String table, String key, String value) {
        if (btreeTables.containsKey(table)) {
            btreeTables.get(table).put(key, value);
        } else if (htreeTables.containsKey(table)) {
            htreeTables.get(table).put(key, value);
        }
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
    }

    public Set<String> keys(String table) {
        if (btreeTables.containsKey(table)) return btreeTables.get(table).getKeys();
        else if (htreeTables.containsKey(table)) return htreeTables.get(table).getKeys();
        return Set.of();
    }

    public void backup() {
        db.commit();
    }
}
