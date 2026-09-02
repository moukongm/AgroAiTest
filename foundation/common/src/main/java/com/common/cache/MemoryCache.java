package com.common.cache;

import android.util.LruCache;

import java.util.Map;

public class MemoryCache<T> {

    private final LruCache<String, T> cache;

    public MemoryCache(int maxSize) {
        this.cache = new LruCache<String, T>(maxSize) {
            @Override
            protected int sizeOf(String key, T value) {
                return 1;
            }
        };
    }

    public T get(String key) {
        return cache.get(key);
    }

    public void setCache(String k, T value) {
        cache.put(k, value);
    }

    public void remove(String key) {
        cache.remove(key);
    }

    public void clear() {
        cache.evictAll();
    }

    public int size() {
        return cache.size();
    }

    public boolean contains(String key) {
        return cache.get(key) != null;
    }
}
