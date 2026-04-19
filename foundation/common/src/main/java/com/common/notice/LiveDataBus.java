package com.common.notice;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class LiveDataBus {
    private static volatile LiveDataBus liveDataBus;

    private final Map<String, MutableLiveData<Object>> map;

    private LiveDataBus() {
        map = new ConcurrentHashMap<>();
    }

    public static LiveDataBus getInstance() {
        if (liveDataBus == null) {
            synchronized (LiveDataBus.class) {
                if (liveDataBus == null) {
                    liveDataBus = new LiveDataBus();
                }
            }
        }
        return liveDataBus;
    }

    @SuppressWarnings("unchecked")
    public <T> MutableLiveData<T> with(String key) {
        return (MutableLiveData<T>) map.computeIfAbsent(key, k -> new MutableLiveData<>());
    }
}
