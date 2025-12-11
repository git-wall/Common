package org.app.common.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Component
public class LocalCacheManager {

    private final Map<String, CacheEntry<?>> cacheStore = new ConcurrentHashMap<>();
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();
    /**
     * Lấy dữ liệu từ cache hoặc gọi API nếu hết hạn
     *
     * @param key Khóa cache
     * @param ttlMinutes Thời gian sống (phút)
     * @param dataSupplier Hàm lấy dữ liệu từ API
     * @return Dữ liệu
     */
    public <T> T get(String key, long ttlMinutes, Supplier<T> dataSupplier) {
        CacheEntry<T> entry = (CacheEntry<T>) cacheStore.get(key);
        LocalDateTime now = LocalDateTime.now();

        // Nếu chưa có cache hoặc đã hết hạn -> gọi API
        if (entry == null || now.isAfter(entry.getExpiryTime())) {
            T data = dataSupplier.get();
            cacheStore.put(key, new CacheEntry<T>(data, now.plusMinutes(ttlMinutes)));
            return data;
        }

        // Slow path: Cần refresh cache -> dùng lock per key
        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            // Double-check: Có thể thread khác đã update rồi
            entry = (CacheEntry<T>) cacheStore.get(key);
            if (entry != null && now.isBefore(entry.getExpiryTime())) {
                return entry.getData();
            }

            // Thực sự cần gọi API -> chỉ thread này làm
            T data = dataSupplier.get();
            cacheStore.put(key, new CacheEntry<>(data, LocalDateTime.now().plusMinutes(ttlMinutes)));
            return data;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Xóa cache theo key
     */
    public void evict(String key) {
        Lock lock = locks.computeIfAbsent(key, k -> new ReentrantLock());
        lock.lock();
        try {
            cacheStore.remove(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Xóa toàn bộ cache
     */
    public void clear() {
        cacheStore.clear();
    }

    /**
     * Entry lưu data và thời gian hết hạn
     */
    @Getter
    @AllArgsConstructor
    private static class CacheEntry<T> {
        private final T data;
        private final LocalDateTime expiryTime;
    }
}
