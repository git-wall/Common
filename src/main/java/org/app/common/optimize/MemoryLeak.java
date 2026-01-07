package org.app.common.optimize;

import org.springframework.stereotype.Service;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MemoryLeak {
    class Event {
        // Event details
    }

    // ✅ Bounded cache
    @Service
    public class EventService {
        private final Queue<Event> events = new ConcurrentLinkedQueue<>();
        private static final int MAX_SIZE = 1000;

        public void addEvent(Event event) {
            events.add(event);
            while (events.size() > MAX_SIZE) {
                events.poll(); // Remove old events
            }
        }
    }

    class Data {
        // Data details
    }

    // ✅ Weak references
    public class CacheService {
        private final Map<String, WeakReference<Data>> cache =
            new ConcurrentHashMap<>();

        public void put(String key, Data data) {
            cache.put(key, new WeakReference<>(data));
        }

        public Data get(String key) {
            WeakReference<Data> ref = cache.get(key);
            return ref != null ? ref.get() : null;
        }
    }
}
