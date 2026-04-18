package com.main.ui.adapter;

import androidx.annotation.NonNull;

import com.main.impl.R;
import com.main.impl.databinding.ItemLetterNavBinding;
import com.uikit.base.BaseBindingAdapter;

public class LetterNavAdapter extends BaseBindingAdapter<String, ItemLetterNavBinding> {

    private String selectedLetter = "";

    public LetterNavAdapter() {
        super();
    }

    @Override
    public void convert(@NonNull ItemLetterNavBinding binding, @NonNull String item, int position) {
        binding.tvLetter.setText(item);
        
        boolean isSelected = item.equals(selectedLetter);
        binding.tvLetter.setTextColor(isSelected ? 0xFF00F0CC : 0xFF333333);
        binding.tvLetter.setBackgroundResource(isSelected ? R.drawable.bg_letter_selected : R.drawable.bg_letter_item);
        
        binding.tvLetter.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    public void setSelectedLetter(String letter) {
        if (!selectedLetter.equals(letter)) {
            selectedLetter = letter;
            notifyDataSetChanged();
        }
    }

    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(String letter);
    }
}
