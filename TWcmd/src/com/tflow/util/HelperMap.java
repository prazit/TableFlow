package com.tflow.util;

import java.util.HashMap;

public class HelperMap<K, V> extends HashMap<K, V> {

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

}
