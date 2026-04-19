package com.main.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.main.impl.R;
import com.main.impl.databinding.ItemCityLetterBinding;
import com.uikit.base.BaseBindingAdapter;

import java.util.Map;

public class CityLetterAdapter extends BaseBindingAdapter<Map.Entry<String, java.util.List<String>>, ItemCityLetterBinding> {

    public CityLetterAdapter() {
        super();
    }

    @Override
    public void convert(@NonNull ItemCityLetterBinding binding, @NonNull Map.Entry<String, java.util.List<String>> item, int position) {
        binding.tvLetter.setText(item.getKey());
    }
}
