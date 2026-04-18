package com.detection.ui.page;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.common.base.BaseFragment;
import com.detection.databinding.FragmentSpeechBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SpeechFragment extends BaseFragment<FragmentSpeechBinding> {
    FragmentSpeechBinding binding;
    @NonNull
    @Override
    public FragmentSpeechBinding getViewBinding(@NotNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSpeechBinding.inflate(inflater,container,false);
    }

    @Override
    public void initView() {
        binding = getBinding();
        binding.aiBack.cvInformationBack.setOnClickListener(v->{
            requireActivity().getSupportFragmentManager().popBackStack();
        });
        binding.btnHangup.setOnClickListener(v->{
            requireActivity().getSupportFragmentManager().popBackStack();
        });
    }

    @Override
    public void initData() {

    }
}
