package org.app.common.utils;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

// Bloom filter for larger list size > 10_000, right has 100% - 0.01% = 99,99%
public class FilterUtils {

    public static <T> boolean filter(Collection<T> list, Function<T, String> funcPut, String val) {
        BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), list.size(), 0.01);
        list.forEach(e -> filter.put(funcPut.apply(e)));
        return filter.mightContain(val);
    }

    public static <T> Collection<T> filterAndRemoveAnyExits(Collection<T> list, Function<T, String> funcPut, String val) {
        BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), list.size(), 0.01);
        list.forEach(e -> filter.put(funcPut.apply(e)));
        list.removeIf(e -> filter.mightContain(val));
        return list;
    }

    public static <T> Optional<T> filterAndFindFirst(Collection<T> list, Function<T, String> funcPut, String val) {
        BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), list.size(), 0.01);
        list.forEach(e -> filter.put(funcPut.apply(e)));
        return list.stream().filter(e -> filter.mightContain(val)).findFirst();
    }

    public static <T> Collection<T> filterAnyListInAndRemove(Collection<T> list, Collection<T> listRemove, Function<T, String> function) {
        BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), list.size(), 0.01);
        listRemove.forEach(e -> filter.put(function.apply(e)));
        list.removeIf(e -> filter.mightContain(function.apply(e)));
        return list;
    }
}
