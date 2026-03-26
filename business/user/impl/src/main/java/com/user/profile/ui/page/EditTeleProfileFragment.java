package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.agri.pest.client.model.request.ChangePhoneRequest;
import com.common.base.BaseFragment;
import com.common.utils.LiveDataExtKt;
import com.user.databinding.FragmentEditnameProfileBinding;
import com.user.profile.viewmodel.ProfileViewModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditTeleProfileFragment extends BaseFragment<FragmentEditnameProfileBinding> {
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
        binding.tvSettitleTitle.setText("修改电话");
        binding.tvSettitleHint.setText("请输入您的电话");

        viewModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);

        binding.cvSettitleBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        binding.tvEditnameOk.setOnClickListener(view -> {
            ChangePhoneRequest request = new ChangePhoneRequest(String.valueOf(binding.etSettitleEdit.getText()));
            binding.tvEditnameOk.setEnabled(false);
            viewModel.updatePhone(request);
        });

        LiveDataExtKt.observeNonNull(viewModel.getPhoneLivedata(), this, mes -> {
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


    }

    @Override
    public void initData() {

    }
}
