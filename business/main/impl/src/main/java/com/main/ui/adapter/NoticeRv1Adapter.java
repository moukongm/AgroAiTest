package com.main.ui.adapter;

import androidx.annotation.NonNull;

import com.main.impl.databinding.ItemNociceRv1Binding;
import com.uikit.base.BaseBindingAdapter;

public class NoticeRv1Adapter extends BaseBindingAdapter<String, ItemNociceRv1Binding> {

    public NoticeRv1Adapter() {
        super();
    }

    @Override
    public void convert(@NonNull ItemNociceRv1Binding binding,
                       @NonNull String item,
                       int position) {
        binding.tvNothaveWarn.setText(item);
    }
}
