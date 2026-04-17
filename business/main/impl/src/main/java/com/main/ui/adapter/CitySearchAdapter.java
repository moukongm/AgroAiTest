package com.main.ui.adapter;

import androidx.annotation.NonNull;

import com.main.impl.databinding.ItemCitySearchResultBinding;
import com.uikit.base.BaseBindingAdapter;

public class CitySearchAdapter extends BaseBindingAdapter<CityInfo, ItemCitySearchResultBinding> {

    public CitySearchAdapter() {
        super();
    }

    @Override
    public void convert(@NonNull ItemCitySearchResultBinding binding, @NonNull CityInfo item, int position) {
        binding.tvCityName.setText(item.name);
        binding.tvCityPinyin.setText(item.pinyin);
        
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
        void onItemClick(CityInfo cityInfo);
    }
}
