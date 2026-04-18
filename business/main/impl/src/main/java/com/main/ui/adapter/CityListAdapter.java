package com.main.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.main.impl.databinding.ItemCityLetterHeaderBinding;
import com.main.impl.databinding.ItemCityNameBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CityListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LETTER_HEADER = 0;
    private static final int TYPE_CITY_NAME = 1;

    private final List<Object> allItems = new ArrayList<>();
    private OnCityClickListener cityClickListener;

    public void setData(List<String> letterNav, Map<String, List<String>> cityDataMap) {
        allItems.clear();
        
        // 添加城市列表（按字母分组）
        for (Map.Entry<String, List<String>> entry : cityDataMap.entrySet()) {
            allItems.add(new LetterHeader(entry.getKey()));
            for (String city : entry.getValue()) {
                allItems.add(new CityItem(city));
            }
        }
        
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = allItems.get(position);
        if (item instanceof LetterHeader) return TYPE_LETTER_HEADER;
        return TYPE_CITY_NAME;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_LETTER_HEADER) {
            ItemCityLetterHeaderBinding binding = ItemCityLetterHeaderBinding.inflate(inflater, parent, false);
            return new LetterHeaderViewHolder(binding);
        }
        ItemCityNameBinding binding = ItemCityNameBinding.inflate(inflater, parent, false);
        return new CityNameViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = allItems.get(position);
        if (holder instanceof LetterHeaderViewHolder) {
            ((LetterHeaderViewHolder) holder).bind((LetterHeader) item);
        } else if (holder instanceof CityNameViewHolder) {
            ((CityNameViewHolder) holder).bind((CityItem) item);
        }
    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }

    class LetterHeaderViewHolder extends RecyclerView.ViewHolder {
        private final ItemCityLetterHeaderBinding binding;

        LetterHeaderViewHolder(ItemCityLetterHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LetterHeader item) {
            binding.tvLetter.setText(item.letter);
        }
    }

    class CityNameViewHolder extends RecyclerView.ViewHolder {
        private final ItemCityNameBinding binding;

        CityNameViewHolder(ItemCityNameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setClickable(true);
        }

        void bind(CityItem item) {
            binding.tvCityName.setText(item.name);
            binding.getRoot().setOnClickListener(v -> {
                if (cityClickListener != null) {
                    cityClickListener.onCityClick(item.name);
                }
            });
        }
    }

    public void setOnCityClickListener(OnCityClickListener listener) {
        this.cityClickListener = listener;
    }

    public interface OnCityClickListener {
        void onCityClick(String cityName);
    }

    public static class LetterHeader {
        public String letter;
        public LetterHeader(String letter) { this.letter = letter; }
    }

    public static class CityItem {
        public String name;
        public CityItem(String name) { this.name = name; }
    }
}
