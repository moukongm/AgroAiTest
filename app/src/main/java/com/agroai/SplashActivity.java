package com.agroai;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.agroai.databinding.ActivitySplashBinding;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.storage.MMKVUtils;
import com.common.utils.ToastUtils;
import com.network.NetworkManager;
import com.user.TokenService;
import com.user.TokenServiceImpl;

import org.jetbrains.annotations.NotNull;

public class SplashActivity extends BaseActivity<ActivitySplashBinding> {

    @Override
    public ActivitySplashBinding getViewBinding() {
        return ActivitySplashBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        checkLogin();
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        checkLogin();
    }

    private void checkLogin() {
        if (TokenService.api().isTokenValid()) {
            NetworkManager.INSTANCE.setToken(MMKVUtils.INSTANCE.custom("user_module").getString("token",""));
            ARouter.getInstance()
                    .build(RouterPath.APP_MAIN_ACTIVITY)
                    .navigation();
        } else {
            ARouter.getInstance()
                    .build(RouterPath.USER_LOGIN_ACTIVITY)
                    .navigation();
        }
        finish();
    }
}
