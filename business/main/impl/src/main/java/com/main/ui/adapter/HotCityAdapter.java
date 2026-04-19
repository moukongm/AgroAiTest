package com.main.ui.adapter;

import androidx.annotation.NonNull;

import com.main.impl.R;
import com.main.impl.databinding.ItemCityHotBinding;
import com.uikit.base.BaseBindingAdapter;

public class HotCityAdapter extends BaseBindingAdapter<String, ItemCityHotBinding> {

    public HotCityAdapter() {
        super();
    }

    @Override
    public void convert(@NonNull ItemCityHotBinding binding, @NonNull String item, int position) {
        binding.tvCityName.setText(item);
        
        binding.getRoot().setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String cityName);
    }
}
