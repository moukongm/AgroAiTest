package com.community.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.community.databinding.ItemSearchHistoryBinding;
import com.uikit.base.BaseBindingAdapter;

public class SearchHistoryAdapter extends BaseBindingAdapter<String, ItemSearchHistoryBinding> {

    private OnItemClickListener onItemClickListener;
    private OnDeleteClickListener onDeleteClickListener;

    @Override
    public void convert(@NonNull ItemSearchHistoryBinding binding,
                        @NonNull String keyword,
                        int position) {
        binding.tvHistoryKeyword.setText(keyword);

        binding.tvHistoryKeyword.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(keyword, position);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String keyword, int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(String keyword, int position);
    }
}