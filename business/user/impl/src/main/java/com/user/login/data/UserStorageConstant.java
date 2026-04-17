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
    public static final String KEY_CUSTOM_CROPS = "custom_crops";
    public static final String KEY_ELDERLY_MODE_SELECTED = "elderly_mode_selected";

    public static final String KEY_MODE_NORMAL = "normal";
    public static final String KEY_MODE_SENIOR = "senior";

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

    public static void saveCustomCrops(String crops) {
        userStorage.put(KEY_CUSTOM_CROPS, crops);
    }

    public static String getCustomCrops() {
        return userStorage.getString(KEY_CUSTOM_CROPS, "");
    }


    public static void saveElderlyModeSelected(boolean selected) {
        userStorage.put(KEY_ELDERLY_MODE_SELECTED, selected);
    }

    public static boolean isElderlyModeSelected() {
        return userStorage.getBoolean(KEY_ELDERLY_MODE_SELECTED, false);
    }

    public static void saveSelectedMode(String mode) {
        userStorage.put("selected_mode", mode);
    }

    public static String getSelectedMode() {
        return userStorage.getString("selected_mode", "");
    }
}