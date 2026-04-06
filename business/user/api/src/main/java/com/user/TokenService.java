package com.user;

import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;

public interface TokenService extends IProvider {
    boolean isTokenValid();

    boolean hasToken();

    boolean isTokenExpired();

    boolean refreshToken();

    static TokenService api() {
        return ARouter.getInstance().navigation(TokenService.class);
    }
}
