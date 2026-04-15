package com.user.profile.ui.page;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.router.RouterPath;
import com.common.utils.FileUtils;
import com.common.utils.ImageLoader;
import com.common.utils.ImagePickerUtil;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.common.utils.ToastUtils;
import com.user.R;
import com.user.databinding.FragmentEditProfileBinding;
import com.user.profile.Utils;
import com.user.profile.viewmodel.ProfileViewModel;

import java.io.File;

@Route(path = RouterPath.USER_EDIT_PROFILE_ACTIVITY)
public class EditProfileActivity extends BaseActivity<FragmentEditProfileBinding> {
    FragmentEditProfileBinding binding;
    ProfileViewModel viewModel;
    private ImagePickerUtil imagePicker;

    @NonNull
    @Override
    public FragmentEditProfileBinding getViewBinding() {
        return FragmentEditProfileBinding.inflate(getLayoutInflater());
    }

    @Override
    public void initView() {
        binding = getBinding();
        // 使用 Activity scope 的 ViewModel，与 ProfileFragment 共享同一个实例
        // 确保 LiveData 数据在两个页面间同步
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        ProfileViewModel vm = viewModel;
        imagePicker = new ImagePickerUtil(this, uri -> {
            File file = FileUtils.INSTANCE.uriToFile(this, uri, getCacheDir());
            if (file != null && file.exists()) {
                vm.uploadAvatar(file);
                showLoading("稍等");
            } else {
                ToastUtils.INSTANCE.showShort(getApplicationContext(), "文件解析失败");
            }
            return null;
        });

        binding.cvInformationBack.setOnClickListener(v -> {
            finish();
        });

        binding.nicknameContainer.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new EditnameProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.passwordContainer.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new EditPasswordProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.cropContainer.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new GuideFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.phoneContainer.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new EditTeleProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        binding.ivArrow1.setOnClickListener(v -> {
            imagePicker.showImageSourceDialog();
        });

        LiveDataExtKt.observeNonNull(viewModel.getAvatarLivedata(), this, observer -> {
            Log.d("ljx", observer);
            ImageLoader.INSTANCE.load(binding.ivSettingTitle, observer);
            // 通知其他页面刷新
            return null;
        });

        LiveDataExtKt.observeNonNull(viewModel.getMesEtAvatarLivedata(), this, observer -> {
            hideLoading();
            Utils.showDialog(this, observer);
            return null;
        });

        LiveDataExtKt.observeNonNull(viewModel.getNickNameLivedata(), this, mes -> {
            binding.tvNicknameValue.setText(mes);
            hideLoading();
            Log.d("pppppp", mes);
            return null;
        });

        LiveDataExtKt.observeNonNull(viewModel.getPhoneValueLivedata(), this, mes -> {
            binding.tvPhoneValue.setText(mes);
            return null;
        });

        LiveDataExtKt.observeNonNull(viewModel.getCropsValueLivedata(), this, mes -> {
            binding.tvCropValue.setText(mes);
            return null;
        });
        viewModel.getProfileUpdatedLivedata().observe(this, mes -> {
            // 只响应 true 的情况，忽略 onDestroy 时的 false 重置
            if (Boolean.TRUE.equals(mes)) {
                LiveDataBus.getInstance().with(BusKey.PROFILE_CHANGED)
                        .setValue(true);
            }
        });
        LiveDataBus.getInstance().with(BusKey.GETUSERPROFILE).observe(this,ob->{
            viewModel.getUserMes();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imagePicker != null) {
            imagePicker.release();
            imagePicker = null;
        }
        // 重置更新标记，避免下次进入时误触发
        viewModel.getProfileUpdatedLivedata().setValue(false);
    }

    @Override
    public void initData() {
        Log.d("pppppp", "data");
        showLoading("加载中...");
        // 获取用户信息（头像、昵称、手机号、关注的作物）
        viewModel.getUserMes();
    }

}
