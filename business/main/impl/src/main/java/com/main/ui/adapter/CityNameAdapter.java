package com.main.ui.adapter;

import androidx.annotation.NonNull;

import com.main.impl.databinding.ItemCityNameBinding;
import com.uikit.base.BaseBindingAdapter;

public class CityNameAdapter extends BaseBindingAdapter<String, ItemCityNameBinding> {

    public CityNameAdapter() {
        super();
    }

    @Override
    public void convert(@NonNull ItemCityNameBinding binding, @NonNull String item, int position) {
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
