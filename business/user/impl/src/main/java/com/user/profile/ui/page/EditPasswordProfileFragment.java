package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.common.base.BaseFragment;
import com.common.utils.LiveDataExtKt;
import com.user.databinding.FragmentEditnameProfileBinding;
import com.user.profile.viewmodel.ProfileViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditPasswordProfileFragment extends BaseFragment<FragmentEditnameProfileBinding> {
    FragmentEditnameProfileBinding binding;
    ProfileViewModel viewModel;

    @NonNull
    @Override
    public FragmentEditnameProfileBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentEditnameProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        binding.tvSettitleTitle.setText("修改密码");
        binding.tvSettitleHint.setText("请输入您的密码");
        Fragment parent = requireParentFragment();
        if (parent instanceof ProfileFragment) {
            viewModel = new ViewModelProvider(parent).get(ProfileViewModel.class);
        } else {
            // 兼容：尝试从爷爷辈获取
            viewModel = new ViewModelProvider(parent.requireParentFragment()).get(ProfileViewModel.class);
        }
        binding.tvEditnameOk.setOnClickListener(view -> {
            binding.tvEditnameOk.setEnabled(false);
            viewModel.updatePassword(String.valueOf(binding.etSettitleEdit.getText()),this);
        });

        LiveDataExtKt.observeNonNull(viewModel.getPasswordLivedata(), this, mes -> {
            binding.tvEditnameOk.setEnabled(true);
            viewModel.showDialog(getContext(), mes);
            if ("修改成功".equals(mes)) {
                getParentFragmentManager().popBackStack();
                Log.d("xzr", mes);
            } else {
                binding.etSettitleEdit.setText("");
            }
            return null;
        });
        binding.cvSettitleBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    @Override
    public void initData() {

    }
}
