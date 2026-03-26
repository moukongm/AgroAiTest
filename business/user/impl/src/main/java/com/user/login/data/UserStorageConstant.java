package com.user.login.data;

import com.common.storage.MMKVInstance;
import com.common.storage.MMKVUtils;

public class UserStorageConstant {
    private static MMKVInstance userStorage = MMKVUtils.INSTANCE.custom("user_module");
    public static final String KEY_TOKEN = "token";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_USER_NAME = "username";
    public static final String KEY_USER_RETOKEN = "refreshToken";
    public static final String KEY_USER_EXPIRESIN = "expiresIn";

    public static final String KEY_PASSWORD = "password";

    public static void saveToken(String token) {
        userStorage.put(KEY_TOKEN, token);
    }

    public static void saveUserId(long userId) {
        userStorage.put(KEY_USER_ID, userId);
    }

    public static void saveUserName(String userName) {
        userStorage.put(KEY_USER_NAME, userName);
    }

    public static void saveRefreshToken(String refreshToken) {
        userStorage.put(KEY_USER_RETOKEN, refreshToken);
    }

    public static void saveExpiresIn(Long expiresIn) {
        userStorage.put(KEY_USER_EXPIRESIN, expiresIn);
    }

    public static void savePassword(String password) {
        userStorage.put(KEY_PASSWORD, password);
    }


    public static String getRefreshToken() {
        return userStorage.getString(KEY_USER_RETOKEN, "");
    }
}