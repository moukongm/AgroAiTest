package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.agri.pest.client.model.request.ChangePhoneRequest;
import com.common.base.BaseFragment;
import com.common.utils.LiveDataExtKt;
import com.user.databinding.FragmentEditnameProfileBinding;
import com.user.profile.Utils;
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
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        binding.bg.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        binding.tvEditnameOk.setOnClickListener(view -> {
            ChangePhoneRequest request = new ChangePhoneRequest(String.valueOf(binding.etSettitleEdit.getText()));
            viewModel.updatePhone(request);
            binding.tvEditnameOk.setEnabled(false);
        });

        LiveDataExtKt.observeNonNull(viewModel.getPhoneLivedata(), this, mes -> {
            binding.tvEditnameOk.setEnabled(true);

            if ("修改成功".equals(mes)) {
                // 修改电话成功，通知 Activity 返回结果
                getParentFragmentManager().popBackStack();
                // 通知 EditProfileActivity 设置 RESULT_OK
                viewModel.getProfileUpdatedLivedata().postValue(true);
                Log.d("xzr", mes);
                Utils.showDialog(getActivity(), "修改成功");
            }
            else if("获取成功".equals(mes)){
            }else {
                if (getActivity() != null) Utils.showDialog(getActivity(), "修改失败");
            }
            binding.etSettitleEdit.setText("");
            return null;
        });


    }

    @Override
    public void initData() {

    }
}
