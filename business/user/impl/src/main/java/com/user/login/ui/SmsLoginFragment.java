package com.user.login.ui;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.response.UserProfileDto;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.ToastUtils;
import com.user.databinding.FragmentSmsloginBinding;
import com.user.login.ui.LoginActivity;
import com.user.login.viewmodel.SmsLoginViewModel;


public class SmsLoginFragment extends BaseFragment<FragmentSmsloginBinding> {
    private SmsLoginViewModel viewModel;
    private LoginFragment loginFragment;

    private final ActivityResultLauncher<Intent> guideLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    // 模式选择完成后，检查作物
                    viewModel.getUserMes();
                } else {
                    navigateToMain();
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropSelectLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // 作物选择完成后，进入主页
                navigateToMain();
            }
    );

    @Override
    public FragmentSmsloginBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentSmsloginBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(SmsLoginViewModel.class);
        getBinding().btnLogin.setOnClickListener(v -> {
            String phone = getBinding().etTel.getText().toString().trim();
            String code = getBinding().etCode.getText().toString().trim();
            viewModel.loginBySms(phone, code);
        });
        getBinding().btnSendCode.setOnClickListener(v -> {
            String phone = getBinding().etTel.getText().toString().trim();
            viewModel.sendCode(phone);
        });
        getBinding().cbAgree.setOnCheckedChangeListener((view, isChecked) -> {
            viewModel.setAgreeChecked(isChecked);
        });
        getBinding().ivPhone.setOnClickListener(v -> {
            if(loginFragment == null){
                loginFragment = new LoginFragment();
                LoginActivity activity = (LoginActivity) getActivity();
                if (activity != null) {
                    activity.replaceFragment(loginFragment);
                }
            }
        });
    }

    @Override
    public void initData() {
        viewModel.getLoginResultLiveData().observe(this, response -> {
            if (response != null && response.getCode() == ServiceCode.SUCCESS) {
                ToastUtils.INSTANCE.showShort(requireContext(), "登录成功:" + response.getMessage());
                ToastUtils.INSTANCE.showShort(requireContext(), "默认密码为:" + response.getData().getGeneratedPassword());
                handleLoginSuccess();
            }
        });

        viewModel.getUserProfileLiveData().observe(this, userProfile -> {
            if (userProfile != null) {
                checkAndNavigateCropSelect(userProfile.getData());
            } else {
                navigateToMain();
            }
        });

        viewModel.getSendCodeLiveData().observe(this, response -> {
            if (response != null && response.getCode() == ServiceCode.SUCCESS) {
                ToastUtils.INSTANCE.showShort(requireContext(), "验证码发送成功:" + response.getMessage());
            }
        });

        viewModel.getErrorLiveData().observe(this, error -> {
            ToastUtils.INSTANCE.showShort(requireContext(), error);
        });

        viewModel.getToastMsg().observe(this, msg -> {
            if (msg != null && !msg.isEmpty()) {
                ToastUtils.INSTANCE.showShort(requireContext(), msg);
            }
        });

        viewModel.getSendCodeBtnText().observe(this, text -> {
            getBinding().btnSendCode.setText(text);
        });
    }

    private void handleLoginSuccess() {
        boolean hasSelectedMode = com.user.login.data.UserStorageConstant.isElderlyModeSelected();

        if (!hasSelectedMode) {
            Log.d("SmsLogin", "未选择过模式，进入模式选择页");
            // 未选择过模式 -> 进入模式选择页
            Intent intent = new Intent(requireContext(), ModeSelectActivity.class);
            guideLauncher.launch(intent);
        } else {
            Log.d("SmsLogin", "已选择过模式，检查作物");
            // 已选择过模式 -> 检查作物
            viewModel.getUserMes();
        }
    }

    private void checkAndNavigateCropSelect(UserProfileDto userProfile) {
        // 检查关注的作物是否为空
        if (userProfile.getFollowedCrops() == null || userProfile.getFollowedCrops().isEmpty()) {
            Intent intent = new Intent(requireContext(), com.user.profile.ui.CropSelectActivity.class);
            intent.putExtra("standalone", true);
            intent.putExtra("returnTo", "login");
            cropSelectLauncher.launch(intent);
        } else {
            navigateToMain();
        }
    }

    private void navigateToMain() {
        ARouter.getInstance()
                .build(RouterPath.APP_MAIN_ACTIVITY)
                .navigation();
        LoginActivity activity = (LoginActivity) getActivity();
        if (activity != null) {
            activity.finish();
        }
    }
}
