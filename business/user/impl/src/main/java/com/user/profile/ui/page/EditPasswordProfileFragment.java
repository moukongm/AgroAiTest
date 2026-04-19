package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;

import com.common.base.BaseFragment;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.utils.LiveDataExtKt;
import com.user.databinding.FragmentEditnameProfileBinding;
import com.user.profile.Utils;
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
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        binding.tvEditnameOk.setOnClickListener(view -> {
            binding.tvEditnameOk.setEnabled(false);
            viewModel.updatePassword(String.valueOf(binding.etSettitleEdit.getText()));
        });

        LiveDataExtKt.observeNonNull(viewModel.getPasswordLivedata(), this, mes -> {
            binding.tvEditnameOk.setEnabled(true);
            Utils.showDialog(getActivity(), mes);
            if ("修改成功".equals(mes)) {
                getParentFragmentManager().popBackStack();
                Log.d("xzr", mes);
            } else {
                binding.etSettitleEdit.setText("");
            }
            return null;
        });
        LiveDataExtKt.observeNonNull(viewModel.getUnLogin(),this,mes ->{
            if("yes".equals(mes)){
                LiveDataBus.getInstance().with(BusKey.UNLOGIN).setValue(true);
                if (isAdded()) requireActivity().finish();
            }
            return null;
        });
        binding.bg.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LiveDataBus.getInstance().with(BusKey.UNLOGIN).setValue(false);
    }

    @Override
    public void initData() {

    }
}
