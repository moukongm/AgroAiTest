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
import com.user.databinding.FragmentLoginBinding;
import com.user.login.viewmodel.LoginViewModel;


public class LoginFragment extends BaseFragment<FragmentLoginBinding> {
    private LoginViewModel viewModel;
    private SmsLoginFragment smsFragment;
    private boolean isFromRegister = false;

    private final ActivityResultLauncher<Intent> guideLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                    viewModel.getUserMes();
                } else {
                    navigateToMain();
                }
            }
    );

    private final ActivityResultLauncher<Intent> cropSelectLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                navigateToMain();
            }
    );

    @Override
    public FragmentLoginBinding getViewBinding(LayoutInflater inflater, ViewGroup container) {
        return FragmentLoginBinding.inflate(inflater,container,false);
    }

    @Override
    public void initView() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        getBinding().btnLogin.setOnClickListener(v -> {
            String phone = getBinding().etPhone.getText().toString().trim();
            String password = getBinding().etPassword.getText().toString().trim();
            isFromRegister = false;
            viewModel.login(phone, password);
        });
        getBinding().btnRegister.setOnClickListener(v -> {
            String phone = getBinding().etPhone.getText().toString().trim();
            String password = getBinding().etPassword.getText().toString().trim();
            isFromRegister = true;
            viewModel.register(phone, password, null);
        });
        getBinding().cbAgree.setOnCheckedChangeListener((view,isChecked) -> {
            viewModel.setAgreeChecked(isChecked);
        });
        getBinding().ivPhone.setOnClickListener(v -> {
            if(smsFragment == null){
                smsFragment = new SmsLoginFragment();
                LoginActivity activity = (LoginActivity) getActivity();
                if (activity != null) {
                    activity.replaceFragment(smsFragment);
                }
            }
        });
    }

    @Override
    public void initData() {
        viewModel.getLoginResultLiveData().observe(this,response -> {
            if (response != null && response.getCode() == ServiceCode.SUCCESS) {
                ToastUtils.INSTANCE.showShort(requireContext(), "登录成功:" + response.getMessage());
                handleLoginSuccess();
            }
        });

        viewModel.getRegisterResultLiveData().observe(this,response -> {
            if(response!=null&&response.getCode()==ServiceCode.SUCCESS){
                ToastUtils.INSTANCE.showShort(requireContext(), "注册成功:" + response.getMessage());
                String phone = getBinding().etPhone.getText().toString().trim();
                String password = getBinding().etPassword.getText().toString().trim();
                isFromRegister = true;
                viewModel.login(phone, password);
            }
        });

        viewModel.getUserProfileLiveData().observe(this, userProfile -> {
            if (userProfile != null) {
                checkAndNavigateCropSelect(userProfile.getData());
            } else {
                navigateToMain();
            }
        });

        viewModel.getErrorLiveData().observe(this,error -> {
            ToastUtils.INSTANCE.showShort(requireContext().getApplicationContext(),error);
        });

        viewModel.getToastMsg().observe(this, msg -> {
            if(msg != null && !msg.isEmpty()){
                ToastUtils.INSTANCE.showShort(requireContext().getApplicationContext(),msg);
            }
        });
    }

    private void handleLoginSuccess() {
        if (isFromRegister) {
            Intent intent = new Intent(requireContext(), ModeSelectActivity.class);
            guideLauncher.launch(intent);
        } else {
            Log.d("LoginFragment", "普通登录，直接进入主页");
            navigateToMain();
        }
    }

    private void checkAndNavigateCropSelect(UserProfileDto userProfile) {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
