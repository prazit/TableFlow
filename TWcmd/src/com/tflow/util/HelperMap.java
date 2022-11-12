package com.tflow.util;

import java.util.HashMap;
import java.util.Map;

public class HelperMap<K, V> extends HashMap<K, V> {

    public HelperMap(Map<? extends K, ? extends V> m) {
        super(m);
    }

    public V get(Object key, V defaultValue) {
        V value = super.get(key);
        if (value == null) return defaultValue;
        return value;
    }

    public Boolean getBoolean(Object key, Boolean defaultValue) {
        V value = super.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Boolean) return (Boolean) value;
        return Boolean.parseBoolean(value.toString());
    }

    public Integer getInteger(Object key, Integer defaultValue) {
        V value = super.get(key);
        if (value == null) return defaultValue;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Double) return ((Double) value).intValue();
        if (value instanceof Boolean) return ((Boolean) value) ? 1 : 0;
        return Integer.parseInt(value.toString());
    }

}
