package com.user;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.router.RouterPath;
import com.common.storage.MMKVInstance;
import com.common.storage.MMKVUtils;
import com.user.login.data.LoginRepository;

@Route(path = RouterPath.TOKEN_SERVICE)
public class TokenServiceImpl implements TokenService {
    private MMKVInstance mmkv;
    private final LoginRepository repository = new LoginRepository();

    @Override
    public void init(Context context) {

    }

    private MMKVInstance getMMKV() {
        if (mmkv == null) {
            mmkv = MMKVUtils.INSTANCE.custom("user_module");
        }
        return mmkv;
    }

    @Override
    public boolean isTokenValid() {
        if (hasToken() && !isTokenExpired()) return true;
        return false;
    }

    @Override
    public boolean hasToken() {
        String token = getMMKV().getString("token", "");
        return !TextUtils.isEmpty(token);
    }

    @Override
    public boolean isTokenExpired() {
        long expiresIn = getMMKV().getLong("expiresIn", 0);
        long nowSecond = System.currentTimeMillis() / 1000;
        return expiresIn <= nowSecond;
    }

    @Override
    public boolean refreshToken() {
        return repository.refreshToken();
    }
}
