package com.user.profile.ui.page;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.webview.WebViewActivity;
import com.common.router.RouterPath;
import com.network.NetworkManager;
import com.user.databinding.FragmentSettingProfileBinding;
import com.user.profile.viewmodel.ProfileViewModel;

@Route(path = RouterPath.USER_SETTING_PROFILE_ACTIVITY)
public class SettingProfileActivity extends BaseActivity<FragmentSettingProfileBinding> {
    private static final String ABOUT_US_URL = NetworkManager.BASE_URL + "about-us.html";
    private static final String FEEDBACK_URL = NetworkManager.BASE_URL + "feedback.html";
    private static final String PERMISSIONS_URL = NetworkManager.BASE_URL + "permissions.html";
    private static final String TERMS_URL = NetworkManager.BASE_URL + "terms.html";
    private static final String PRIVACY_URL = NetworkManager.BASE_URL + "privacy.html";

    FragmentSettingProfileBinding binding;
    ProfileViewModel viewModel;

    @NonNull
    @Override
    public FragmentSettingProfileBinding getViewBinding() {
        return FragmentSettingProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        binding = getBinding();
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.initContext(this);

        binding.cvSettingprofileBack.setOnClickListener(v -> {
            finish();
        });
        binding.setAbout.setOnClickListener(v -> openWebPage("关于我们", ABOUT_US_URL));
        binding.setTickle.setOnClickListener(v -> openWebPage("我要反馈", FEEDBACK_URL));
        binding.setPower.setOnClickListener(v -> openWebPage("系统权限", PERMISSIONS_URL));
        binding.setAgreement.setOnClickListener(v -> openWebPage("用户协议", TERMS_URL));
        binding.setPolicy.setOnClickListener(v -> openWebPage("隐私政策", PRIVACY_URL));
        binding.btnSettingUnlogin.setOnClickListener(v -> {
            viewModel.unLogin();
        });
        LiveDataExtKt.observeNonNull(viewModel.getUnLogin(),this,mes ->{
            if("yes".equals(mes)){
                LogUtils.INSTANCE.d("unlogin",mes);
                LiveDataBus.getInstance().with(BusKey.UNLOGIN).setValue(true);
                finish();
            }
            return null;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LiveDataBus.getInstance().with(BusKey.UNLOGIN).setValue(false);
    }

    @Override
    public void initData() {
        // 加载用户信息
        viewModel.getUserMes();
    }

    private void openWebPage(String title, String url) {
        WebViewActivity.Companion.start(this, url, title);
    }

}
