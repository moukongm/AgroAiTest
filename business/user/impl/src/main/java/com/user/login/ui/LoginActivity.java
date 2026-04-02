package com.user.login.ui;

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
    private LoginFragment loginFragment;

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

    public void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main, fragment)
                    .commit();
        }
    }

    private void autoRefreshToken() {
        TokenService service = TokenService.api();

        if (service.hasToken() && service.isTokenExpired()) {

            ThreadUtils.INSTANCE.executeByIo(() -> {
                boolean success = service.refreshToken();

                if (success) {
                    ThreadUtils.INSTANCE.runOnUiThread(() -> {
                        ARouter.getInstance()
                                .build(RouterPath.HOME_FRAGMENT)
                                .navigation(LoginActivity.this);
                        finish();
                    });
                }
                if (loginFragment == null) {
                    loginFragment = new LoginFragment();
                    replaceFragment(loginFragment);
                }
            });
        } else {
            if (loginFragment == null) {
                loginFragment = new LoginFragment();
                replaceFragment(loginFragment);
            }
        }
    }
}
