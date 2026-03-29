package com.user.profile.ui.page;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.common.base.BaseFragment;
import com.common.utils.ImageLoader;
import com.common.utils.ImagePickerUtil;
import com.common.utils.LiveDataExtKt;
import com.common.utils.LogUtils;
import com.user.R;
import com.user.databinding.FragmentEditProfileBinding;
import com.user.profile.viewmodel.ProfileViewModel;


public class EditProfileFragment extends BaseFragment<FragmentEditProfileBinding> {
    FragmentEditProfileBinding binding;

    ProfileViewModel viewModel;
    private ImagePickerUtil imagePicker;


    EditnameProfileFragment editnameProfileFragment;

    @NonNull
    @Override
    public FragmentEditProfileBinding getViewBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentEditProfileBinding.inflate(inflater, container, false);
    }

    @Override
    public void initView() {

        binding = getBinding();
        viewModel = new ViewModelProvider(getActivity()).get(ProfileViewModel.class);

        ProfileViewModel vm = viewModel;
        final Context appContext = requireActivity().getApplicationContext();
        imagePicker = new ImagePickerUtil(requireActivity(), uri -> {
            vm.getUnloadAvatar(appContext, uri);
            return null;
        });

        binding.cvInformationBack.setOnClickListener(v -> {
            viewModel.popBackstackFragment(this);
        });
        //修改昵称界面
        binding.nicknameContainer.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new EditnameProfileFragment())
                    .addToBackStack(null)
                    .commit();
            LogUtils.INSTANCE.d("ljx", "到底能不能点");

        });
        binding.passwordContainer.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new EditPasswordProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });
        binding.cropContainer.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new GuideFragment())
                    .addToBackStack(null)
                    .commit();
        });
        binding.phoneContainer.setOnClickListener(v -> {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.fl_editprofile, new EditTeleProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });
        //选择
        binding.ivArrow1.setOnClickListener(v -> {
            imagePicker.showImageSourceDialog();
//            binding.editProfile.show();
        });

        //修改照片成功
        LiveDataExtKt.observeNonNull(viewModel.getAvatarLivedata(), this, observer -> {
            //记得通知其他有用到头像的地方
            //本地数据库也没改
            Log.d("ljx", observer);
            ImageLoader.INSTANCE.load(binding.ivSettingTitle, observer);
//            binding.editProfile.hide();
            return null;
        });

        //修改结果
        LiveDataExtKt.observeNonNull(viewModel.getMesEtAvatarLivedata(), this, observer -> {
            viewModel.showDialog(getContext(), observer);
            return null;
        });

        LiveDataExtKt.observeNonNull(viewModel.getNickNameLivedata(), this, mes -> {
            binding.tvNicknameValue.setText(mes);
            return null;
        });

        LiveDataExtKt.observeNonNull(viewModel.getPhoneValueLivedata(), this, mes -> {
            binding.tvPhoneValue.setText(mes);
            return null;
        });


        LiveDataExtKt.observeNonNull(viewModel.getCropsValueLivedata(), this, mes -> {
            binding.tvCropValue.setText(mes);
            return null;
        });
    }

    @Override
    public void initData() {
        //无法确定数据来源
    }
}