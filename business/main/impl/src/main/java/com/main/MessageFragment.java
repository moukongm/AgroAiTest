package com.main;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.viewbinding.ViewBinding;

import com.common.base.BaseFragment;
import com.main.impl.databinding.FragmentMessageBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageFragment extends BaseFragment<FragmentMessageBinding> {
    @Override
    public @NotNull FragmentMessageBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMessageBinding.inflate(inflater,container,false);
    }

    @Override
    public void initData() {

    }

    @Override
    public void initView() {

    }
}
