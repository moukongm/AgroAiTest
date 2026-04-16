package com.user.profile.ui.page;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.user.databinding.FragmentSettingProfileBinding;
import com.user.profile.viewmodel.ProfileViewModel;

@Route(path = RouterPath.USER_SETTING_PROFILE_ACTIVITY)
public class SettingProfileActivity extends BaseActivity<FragmentSettingProfileBinding> {
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

}
