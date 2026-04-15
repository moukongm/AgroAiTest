package com.user.profile.ui.page;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.common.base.BaseFragment;
import com.common.utils.LiveDataExtKt;
import com.user.databinding.FragmentEditnameProfileBinding;
import com.user.profile.Utils;
import com.user.profile.viewmodel.ProfileViewModel;

public class EditnameProfileFragment extends BaseFragment<FragmentEditnameProfileBinding> {
    FragmentEditnameProfileBinding binding;
    ProfileViewModel viewModel;

    @NonNull
    @Override
    public FragmentEditnameProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentEditnameProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {
        Log.d("ljx", "initview");
        binding = getBinding();
        viewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);
        //得到数据库中的数据后更换这里的电话

        binding.tvEditnameOk.setOnClickListener(view -> {
            Log.d("ljx", "ok");
            binding.tvEditnameOk.setEnabled(false);
            ProfileUpdateRequest request = new ProfileUpdateRequest(String.valueOf(binding.etSettitleEdit.getText()),
                    null, null, null, null);
            viewModel.updataProfile(request, "fullName");
        });

        //是否修改昵称成功
        LiveDataExtKt.observeNonNull(viewModel.getMesEtnameLivedata(), this, mes -> {
            binding.editnameProfile.setVisibility(View.GONE);
            binding.tvEditnameOk.setEnabled(true);
            Utils.showDialog(getContext(), mes);
//            ToastUtils.INSTANCE.showShort(getActivity().getBaseContext(),mes);
            if ("修改成功".equals(mes)) {
                getParentFragmentManager().popBackStack();
                Log.d("xzr", mes);
            } else {
                binding.etSettitleEdit.setText("");
            }
            return null;
        });

        //能否修改昵称
        LiveDataExtKt.observeNonNull(viewModel.getMesNameLivedata(), this, mes -> {
            if ("ok".equals(mes)) {
                binding.editnameProfile.setVisibility(View.VISIBLE);
                binding.tvEditnameOk.setEnabled(false);
            } else {
                binding.etSettitleEdit.setText("");
                Utils.showDialog(getContext(), mes);
//                viewModel.showDialog(getContext(), mes);
            }

            return null;
        });


        binding.bg.cvInformationBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

    }


    @Override
    public void initData() {

    }
}