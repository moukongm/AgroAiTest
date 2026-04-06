package com.user.login.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;

import com.agri.pest.client.api.ServiceCode;
import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.router.RouterPath;
import com.common.utils.ToastUtils;
import com.user.databinding.FragmentLoginBinding;
import com.user.login.viewmodel.LoginViewModel;

public class LoginFragment extends BaseFragment<FragmentLoginBinding> {
    private LoginViewModel viewModel;

    private SmsLoginFragment fragment;

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
            viewModel.login(phone, password);
        });


        getBinding().btnRegister.setOnClickListener(v -> {
            String phone = getBinding().etPhone.getText().toString().trim();
            String password = getBinding().etPassword.getText().toString().trim();
            viewModel.register(phone, password, null);
        });
        getBinding().cbAgree.setOnCheckedChangeListener((view,isChecked) -> {
            viewModel.setAgreeChecked(isChecked);
        });
        getBinding().ivPhone.setOnClickListener(v -> {
            if(fragment == null){
                fragment = new SmsLoginFragment();
                LoginActivity activity = (LoginActivity) getActivity();
                activity.replaceFragment(fragment);
            }
        });
    }

    @Override
    public void initData() {
        viewModel.getLoginResultLiveData().observe(this,response -> {
            if (response != null && response.getCode() == ServiceCode.SUCCESS) {
                ToastUtils.INSTANCE.showShort(requireContext(), "登录成功:" + response.getMessage());
                ARouter.getInstance()
                        .build(RouterPath.APP_MAIN_ACTIVITY)
                        .navigation();
                LoginActivity activity = (LoginActivity) getActivity();
                activity.finish();
            }
        });

        viewModel.getRegisterResultLiveData().observe(this,response -> {
            if(response!=null&&response.getCode()==ServiceCode.SUCCESS){
                ToastUtils.INSTANCE.showShort(requireContext(), "注册成功:" + response.getMessage());
            }
        });

        viewModel.getErrorLiveData().observe(this,error -> {
            ToastUtils.INSTANCE.showShort(requireContext(),error);
        });

        viewModel.getToastMsg().observe(this, msg -> {
            if(msg != null && !msg.isEmpty()){
                ToastUtils.INSTANCE.showShort(requireContext(),msg);
            }
        });
    }
}
