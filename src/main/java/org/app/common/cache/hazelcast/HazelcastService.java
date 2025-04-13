package org.app.common.cache.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class HazelcastService {

    private final HazelcastInstance hazelcastInstance;

    public <K, V> void put(String mapName, K key, V value) {
        IMap<K, V> map = hazelcastInstance.getMap(mapName);
        map.put(key, value);
    }

    public <K, V> void putWithTtl(String mapName, K key, V value, long ttl, TimeUnit timeUnit) {
        IMap<K, V> map = hazelcastInstance.getMap(mapName);
        map.put(key, value, ttl, timeUnit);
    }

    public <K, V> V get(String mapName, K key) {
        IMap<K, V> map = hazelcastInstance.getMap(mapName);
        return map.get(key);
    }

    public <K> void remove(String mapName, K key) {
        IMap<K, Object> map = hazelcastInstance.getMap(mapName);
        map.remove(key);
    }

    public <K> boolean containsKey(String mapName, K key) {
        IMap<K, Object> map = hazelcastInstance.getMap(mapName);
        return map.containsKey(key);
    }

    public void clearMap(String mapName) {
        hazelcastInstance.getMap(mapName).clear();
    }

    public <K, V> IMap<K, V> getMap(String mapName) {
        return hazelcastInstance.getMap(mapName);
    }
}