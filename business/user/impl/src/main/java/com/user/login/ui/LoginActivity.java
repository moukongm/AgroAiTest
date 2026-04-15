package com.user.login.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.router.RouterPath;
import com.common.utils.ThreadUtils;
import com.user.R;
import com.user.TokenService;
import com.user.databinding.ActivityLoginBinding;

@Route(path = RouterPath.USER_LOGIN_ACTIVITY)
public class LoginActivity extends BaseActivity<ActivityLoginBinding> {

    private static final int CONTAINER_ID = R.id.activity_main;
    private volatile boolean isDestroyed = false;

    @Override
    public ActivityLoginBinding getViewBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        autoRefreshToken();
    }

    @Override
    public void initData() {
    }

    public void replaceFragment(@NonNull Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(CONTAINER_ID, fragment)
                .commit();
    }

    private void autoRefreshToken() {
        TokenService service = TokenService.api();

        if (service.hasToken() && service.isTokenExpired()) {

            ThreadUtils.INSTANCE.executeByIo(() -> {
                boolean success = service.refreshToken();

                if (isDestroyed) {
                    return;
                }

                if (success) {
                    ThreadUtils.INSTANCE.runOnUiThread(() -> {
                        if (isDestroyed) {
                            return;
                        }
                        ARouter.getInstance()
                                .build(RouterPath.APP_MAIN_ACTIVITY)
                                .navigation(LoginActivity.this);
                        finish();
                    });
                } else {
                    ThreadUtils.INSTANCE.runOnUiThread(() -> {
                        if (isDestroyed) {
                            return;
                        }
                        navigateToLogin();
                    });
                }
            });
        } else {
            navigateToLogin();
        }
    }

    private void navigateToLogin() {
        Fragment current = getSupportFragmentManager().findFragmentById(CONTAINER_ID);
        if (current == null || !(current instanceof LoginFragment)) {
            replaceFragment(new LoginFragment());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
    }
}
