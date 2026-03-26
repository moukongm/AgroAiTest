package com.user.profile.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.common.base.BaseFragment;
import com.user.databinding.FragmentSettingProfileBinding;
import com.user.profile.viewmodel.ProfileViewModel;

public class SettingProfileFragment extends BaseFragment<FragmentSettingProfileBinding> {
    FragmentSettingProfileBinding binding;
    ProfileViewModel viewModel;

    @NonNull
    @Override
    public FragmentSettingProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSettingProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void initData() {

        binding = getBinding();
        viewModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);

        binding.cvSettingprofileBack.setOnClickListener(v -> {
            viewModel.popBackstackFragment(this);
        });
        binding.btnSettingUnlogin.setOnClickListener(v -> {
            viewModel.unLogin();
        });
    }

    @Override
    public void initView() {

    }
}
