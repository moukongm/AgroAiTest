package com.common.notice;

import androidx.lifecycle.MutableLiveData;

import com.common.utils.SingleLiveEvent;

import java.util.HashMap;
import java.util.Map;

public class LiveDataBus {
    private static LiveDataBus liveDataBus;

    private final Map<String, MutableLiveData<Object>> map;

    public LiveDataBus() {
        map = new HashMap<>();
    }

    public static LiveDataBus getInstance() {
        if(liveDataBus == null){
            liveDataBus = new LiveDataBus();
        }
        return liveDataBus;
    }

    public <T> MutableLiveData<T> with(String key){
        if(!map.containsKey(key)){
            map.put(key,new MutableLiveData<>());
        }
        return (MutableLiveData<T>) map.get(key);
    }

}
