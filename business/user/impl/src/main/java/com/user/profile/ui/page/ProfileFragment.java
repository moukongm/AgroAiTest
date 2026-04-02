package com.user.profile.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.common.base.BaseFragment;
import com.user.databinding.ActivityProfileBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ProfileFragment extends BaseFragment<ActivityProfileBinding> {


    @Override
    public @NotNull ActivityProfileBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return ActivityProfileBinding.inflate(inflater,container,false);
    }

    @Override
    public void initView() {

    }

    @Override
    public void initData() {

    }
}
