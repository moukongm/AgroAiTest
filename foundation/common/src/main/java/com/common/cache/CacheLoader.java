package com.common.cache;

import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class CacheLoader<T> {

    private static final String TAG = "CacheLoader";
    private final MemoryCache<T> memoryCache;
    private final DiskCache<T> diskCache;
    private CacheLoader(Builder<T> builder) {
        this.memoryCache = builder.memoryCache;
        this.diskCache = builder.diskCache;
    }
    public static <T> Builder<T> create() {
        return new Builder<>();
    }

    public T get(String key) {

        if (memoryCache != null) {
            T result = memoryCache.get(key);
            if (result != null) {
                return result;
            }
        }

        if (diskCache != null) {
            T result = diskCache.get(key);
            if (result != null) {
                if (memoryCache != null) {
                    memoryCache.setCache(key, result);
                }
                return result;
            }
        }

        return null;
    }


    public void put(String key, T value) {
        if(memoryCache != null) {
            memoryCache.setCache(key, value);
        }
        if(diskCache != null) {
            diskCache.setCache(key, value);
        }
    }

    public void remove(String key) {
        if (memoryCache != null) {
            memoryCache.remove(key);
        }
        if (diskCache != null) {
            diskCache.remove(key);
        }
    }

    public void clear() {
        if (memoryCache != null) {
            memoryCache.clear();
        }
        if (diskCache != null) {
            diskCache.clear();
        }
    }



    public static class Builder<T> {
        private MemoryCache<T> memoryCache;
        private DiskCache<T> diskCache;
        public Builder<T> memory(MemoryCache<T> cache) {
            this.memoryCache = cache;
            return this;
        }
        public Builder<T> memory(int maxSize) {
            this.memoryCache = new MemoryCache<>(maxSize);
            return this;
        }
        public Builder<T> disk(DiskCache<T> cache) {
            this.diskCache = cache;
            return this;
        }

        public CacheLoader<T> build() {
            return new CacheLoader<>(this);
        }
    }
}
