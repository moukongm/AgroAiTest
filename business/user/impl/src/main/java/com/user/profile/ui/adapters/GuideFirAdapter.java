package com.user.profile.ui.adapters;

import androidx.annotation.NonNull;

import com.uikit.base.BaseBindingAdapter;
import com.user.databinding.ItemGuideRegisterBinding;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GuideFirAdapter extends BaseBindingAdapter<String,ItemGuideRegisterBinding> {
    public GuideFirAdapter(int customLayoutResId, @Nullable List<String> data) {
        super(customLayoutResId, data);
    }

    @Override
    public void convert(@NonNull ItemGuideRegisterBinding binding, String item, int position) {
        binding.chipMelon.setText(item);
    }
}
