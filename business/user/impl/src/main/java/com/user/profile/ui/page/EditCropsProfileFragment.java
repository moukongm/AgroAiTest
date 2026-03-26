package com.user.profile.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.common.base.BaseFragment;
import com.user.databinding.FragmentEditnameProfileBinding;
import com.user.profile.viewmodel.ProfileViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditCropsProfileFragment extends BaseFragment<FragmentEditnameProfileBinding> {
    FragmentEditnameProfileBinding binding;
    ProfileViewModel viewModel;
    @NonNull
    @Override
    public FragmentEditnameProfileBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentEditnameProfileBinding.inflate(inflater,container,false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        binding.tvSettitleTitle.setText("修改作物");
        binding.tvSettitleHint.setText("请输入您关注的作物");

        viewModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);

        binding.tvEditnameOk.setOnClickListener(view -> {
        });
    }

    @Override
    public void initData() {

    }
}
