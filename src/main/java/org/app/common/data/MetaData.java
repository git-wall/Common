package org.app.common.data;

import io.vavr.Function2;
import org.app.common.option.Provider;
import org.thymeleaf.util.ListUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.app.common.option.Provider.innerMapper;
import static org.app.common.option.Provider.mapper;

public class MetaData {
    private final Map<Object, Object> data;
    private MetaData holder;

    public MetaData() {
        data = new HashMap<>(8);
    }

    public MetaData(Object key, Object value) {
        data = new HashMap<>(8);
        this.data.put(key, value);
    }

    public static MetaData of(Object key, Object value) {
        return new MetaData(key, value);
    }

    public MetaData put(Object key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public MetaData replace(Object key, Object value) {
        this.put(key, value);
        return this;
    }

    public MetaData transform(Object key, Function<? super Object, ?> function) {
        Object value = this.get(key);
        this.put(key, function.apply(value));
        return this;
    }

    public void remove(Object key) {
        this.data.remove(key);
    }

    public MetaData activeHolder() {
        this.holder = new MetaData();
        return this;
    }

    public MetaData putHolder(Object key, Object value) {
        this.holder.put(key, value);
        return this;
    }

    public MetaData removeHolder(Object key) {
        this.holder.remove(key);
        return this;
    }

    public MetaData clearHolder() {
        this.holder.data.clear();
        this.holder = null;
        return this;
    }

    public <T, R> MetaData putHolderFrom(Object key, Function<T, R> mapper, Object keyHolder) {
        this.holder.put(keyHolder, this.getVal(this.get(key), mapper));
        return this;
    }

    public <T, R> MetaData putFromHolder(Object keyHolder, Function<T, R> mapper, Object key) {
        this.data.put(key, this.getVal(this.holder.get(keyHolder), mapper));
        return this;
    }

    private <T, R> Object getVal(T val, Function<T, R> mapper) {
        return val instanceof List ? innerMapper((List) val, mapper) : mapper(val, mapper);
    }

    public <T> MetaData innerFilter(Object key, Predicate<T> predicate) {
        Object value = Provider.innerFilter(this.get(key), predicate);
        this.data.put(key, value);
        return this;
    }

    public <T> T get(Object key) {
        return (T) this.data.get(key);
    }

    public <T> T get(Object key, Class<T> clazz) {
        return clazz.cast(this.data.get(key));
    }

    public <T, R> R as(Object key, Function<T, R> mapper) {
        return mapper.apply(get(key));
    }

    public <T, R> R as(Object key, Function2<T, MetaData, R> mapper) {
        return mapper.apply(get(key), this);
    }

    public <T, R> List<R> asList(Object key, Function2<T, MetaData, List<R>> mapper) {
        return mapper.apply(get(key), this);
    }

    public List<?> asList(Object key) {
        return ListUtils.toList(this.data.get(key));
    }

    public <T, R> List<R> asListMapper(Object key, Function2<T, MetaData, R> mapper) {
        return ((List<T>) get(key)).stream().map(t -> mapper.apply(t, this)).collect(Collectors.toList());
    }

    public Map<?, ?> asMap(Object key) {
        return (Map<?, ?>) this.data.get(key);
    }
}


