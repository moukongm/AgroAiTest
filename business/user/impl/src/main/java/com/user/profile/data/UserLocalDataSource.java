package com.user.profile.data;

import com.common.storage.MMKVInstance;
import com.common.storage.MMKVUtils;
import com.tencent.mmkv.MMKV;
import com.user.login.data.UserStorageConstant;

public class UserLocalDataSource {
    private final MMKVInstance mmkv = MMKVUtils.INSTANCE.custom("user_module");

    public void unLogin() {
        mmkv.clear();
    }
    public void updateTele(String newTele) {

        mmkv.put(UserStorageConstant.KEY_USER_EXPIRESIN,newTele);
    }
    public void updateNickname(String newNickname) {
        mmkv.put(UserStorageConstant.KEY_USER_NAME,newNickname);

    }
    public void updatePassword(String newPassword) {
        mmkv.put(UserStorageConstant.KEY_PASSWORD,newPassword);
    }

}

