package com.community.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.community.databinding.ItemSearchSuggestionBinding;
import com.uikit.base.BaseBindingAdapter;

public class SearchSuggestionAdapter extends BaseBindingAdapter<String, ItemSearchSuggestionBinding> {

    private OnItemClickListener onItemClickListener;

    public SearchSuggestionAdapter() {
        super();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public void convert(@NonNull ItemSearchSuggestionBinding binding,
                        @NonNull String item,
                        int position) {
        android.util.Log.d("SuggestionAdapter", "convert called! item=" + item + " position=" + position);
        binding.tvKeyword.setText(item);

        binding.getRoot().setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item, position);
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(String keyword, int position);
    }
}
