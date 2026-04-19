package com.user.profile.data;

import android.app.Application;
import android.content.Context;

import com.common.storage.MMKVInstance;
import com.common.storage.MMKVUtils;
import com.common.storage.database.AppDatabase;
import com.common.storage.database.ChatDao;
import com.common.storage.database.CropDao;
import com.common.storage.database.DetectionDao;
import com.common.storage.database.PostDao;
import com.common.storage.database.SearchHistoryDao;
import com.common.storage.database.UserDao;
import com.common.storage.database.PostEntity;
import com.common.storage.database.UserRecord;
import com.common.utils.LogUtils;
import com.tencent.mmkv.MMKV;
import com.user.login.data.UserStorageConstant;

import java.util.List;
import java.util.concurrent.Executors;

public class UserLocalDataSource {
    private final MMKVInstance mmkv = MMKVUtils.INSTANCE.custom("user_module");

    private final Context appContext;

    public UserLocalDataSource() {
        this(null);
    }

    public UserLocalDataSource(Context context) {
        this.appContext = context != null ? context.getApplicationContext() : null;
    }

    public void unLogin() {
        mmkv.remove("token");
        mmkv.remove("userId");
        mmkv.remove("username");
        mmkv.remove("refreshToken");
        mmkv.remove("expiresIn");
        mmkv.remove("password");
        mmkv.remove("custom_crops");
        mmkv.remove("crop_selection");

        if (appContext == null) {
            LogUtils.INSTANCE.d("UserLocalDataSource", "appContext is null, skip clearing database");
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.Companion.getInstance(appContext);
                try {
                    db.cropDao().deleteAll();
                } catch (Exception e) {
                    LogUtils.INSTANCE.d("UserLocalDataSource", "cropDao deleteAll failed: " + e.getMessage());
                }
                try {
                    db.searchHistoryDao().deleteAll();
                } catch (Exception e) {
                    LogUtils.INSTANCE.d("UserLocalDataSource", "searchHistoryDao deleteAll failed: " + e.getMessage());
                }
                try {
                    db.detectionDao().deleteAll();
                } catch (Exception e) {
                    LogUtils.INSTANCE.d("UserLocalDataSource", "detectionDao deleteAll failed: " + e.getMessage());
                }
                try {
                    db.userDao().deleteAll();
                } catch (Exception e) {
                    LogUtils.INSTANCE.d("UserLocalDataSource", "userDao deleteAll failed: " + e.getMessage());
                }
                LogUtils.INSTANCE.d("UserLocalDataSource", "所有用户本地数据已清除");
            } catch (Exception e) {
                LogUtils.INSTANCE.e("UserLocalDataSource", "清除数据失败", e);
            }
        });
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

    public void updateAvatarLocalPath(String localPath) {
        long userId = 0;
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.Companion.getInstance(appContext);
                UserDao userDao = db.userDao();
                UserRecord user = userDao.getUserById(userId);
                if (user != null) {
                    user.setAvatarLocalPath(localPath);
                    userDao.update(user);
                    LogUtils.INSTANCE.d("UserLocalDataSource", "avatar local path updated: " + localPath);
                } else {
                    UserRecord newUser = new UserRecord();
                    newUser.setUserId(userId);
                    newUser.setAvatarLocalPath(localPath);
                    userDao.insert(newUser);
                    LogUtils.INSTANCE.d("UserLocalDataSource", "user created with avatar local path: " + localPath);
                }
            } catch (Exception e) {
                LogUtils.INSTANCE.e("UserLocalDataSource", "update avatar local path failed", e);
            }
        });
    }

    public UserRecord getLocalUser() {
        long userId = 0;
        try {
            if (appContext == null) {
                LogUtils.INSTANCE.d("UserLocalDataSource", "appContext is null");
                return null;
            }
            AppDatabase db = AppDatabase.Companion.getInstance(appContext);
            return db.userDao().getUserById(userId);
        } catch (Exception e) {
            LogUtils.INSTANCE.e("UserLocalDataSource", "get local user failed", e);
            return null;
        }
    }

    public int getLocalDetectionCount() {
        try {
            if (appContext == null) {
                LogUtils.INSTANCE.d("UserLocalDataSource", "appContext is null");
                return 0;
            }
            AppDatabase db = AppDatabase.Companion.getInstance(appContext);
            return db.detectionDao().getCount();
        } catch (Exception e) {
            LogUtils.INSTANCE.e("UserLocalDataSource", "get detection count failed", e);
            return 0;
        }
    }

    public String getLocalNickname() {
        return mmkv.getString(UserStorageConstant.KEY_USER_NAME, "");
    }

}

