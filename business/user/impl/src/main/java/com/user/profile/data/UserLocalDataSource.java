package com.user.profile.data;

import com.common.storage.MMKVInstance;
import com.common.storage.MMKVUtils;
import com.tencent.mmkv.MMKV;

public class UserLocalDataSource {
    private final MMKVInstance mmkv = MMKVUtils.INSTANCE.custom("user_module");

    public void unLogin(){
        mmkv.clear();
    }

}

