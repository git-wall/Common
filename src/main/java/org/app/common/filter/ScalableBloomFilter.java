package org.app.common.filter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnel;

import java.util.ArrayList;
import java.util.List;

// Faster check data already exists
public class ScalableBloomFilter<T> {

    private final List<BloomFilter<T>> filters = new ArrayList<>(8);
    private final double initialFpp;
    private final int initialCapacity;
    private final Funnel<T> funnel;

    public ScalableBloomFilter(int capacity, Funnel<T> funnel) {
        this.initialCapacity = capacity;
        this.initialFpp = 0.02;
        this.funnel = funnel;
        filters.add(createFilter(capacity, 0.02));
    }

    public ScalableBloomFilter(int capacity, double fpp, Funnel<T> funnel) {
        this.initialCapacity = capacity;
        this.initialFpp = fpp;
        this.funnel = funnel;
        filters.add(createFilter(capacity, fpp));
    }

    /**
     * <pre>
     * ✅ If item looks like it's not in the Bloom Filter (very likely a new one)
     * 🔁 Then add a new filter with tighter FPP and bigger capacity (scale)
     * </pre>
     * @param item new item to add
     * */
    public void add(T item) {
        if (!filters.get(filters.size() - 1).mightContain(item)) {
            BloomFilter<T> newFilter = createFilter(initialCapacity << 1, initialFpp / 2.0);
            newFilter.put(item);
            filters.add(newFilter);
        }
        filters.get(filters.size() - 1).put(item);
    }

    public boolean mightContain(T item) {
        return filters.stream().anyMatch(f -> f.mightContain(item));
    }

    private BloomFilter<T> createFilter(int capacity, double fpp) {
        return BloomFilter.create(funnel, capacity, fpp);
    }
}