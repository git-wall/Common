package org.app.common.data;

import org.app.common.pattern.legacy.FluentApi;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DataHolder extends FluentApi<DataHolder> {
    private final Map<String, Object> holder;

    public DataHolder(Map<String, Object> holder) {
        super();
        this.holder = holder;
    }

    public static DataHolder of() {
        return new DataHolder(new HashMap<>(8, 0.75f));
    }

    public DataHolder put(String key, Object value) {
        holder.put(key, value);
        return self();
    }

    public DataHolder remove(String key) {
        holder.remove(key);
        return self();
    }

    public DataHolder update(String key, Object value) {
        holder.put(key, value);
        return self();
    }

    public DataHolder clear() {
        holder.clear();
        return self();
    }

    public DataHolder putNextWithKey(String key, String nextKey, Function<Object, Object> function) {
        holder.put(nextKey, function.apply(holder.get(key)));
        return self();
    }

    public <T> T get(String key) {
        return (T) holder.get(key);
    }
}
