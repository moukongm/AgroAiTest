package com.user.login.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.agri.pest.client.api.ServiceCode;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.ToastUtils;
import com.user.databinding.FragmentSmsloginBinding;
import com.user.login.ui.LoginActivity;
import com.user.login.viewmodel.SmsLoginViewModel;


public class SmsLoginFragment extends BaseFragment<FragmentSmsloginBinding> {
    private SmsLoginViewModel viewModel;

    private LoginFragment fragment;

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
            if(fragment == null){
                fragment = new LoginFragment();
                LoginActivity activity = (LoginActivity) getActivity();
                activity.replaceFragment(fragment);
            }
        });
    }

    @Override
    public void initData() {
        viewModel.getLoginResultLiveData().observe(this, response -> {
            if (response != null && response.getCode() == ServiceCode.SUCCESS) {
                ToastUtils.INSTANCE.showShort(requireContext(), "登录成功:" + response.getMessage());
                ToastUtils.INSTANCE.showShort(requireContext(), "默认密码为:" + response.getData().getGeneratedPassword());
                ARouter.getInstance()
                        .build(RouterPath.HOME_FRAGMENT)
                        .navigation();
                LoginActivity activity = (LoginActivity) getActivity();
                activity.finish();
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
}
