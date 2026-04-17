package com.main.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.main.impl.databinding.ItemCityLetterHeaderBinding;
import com.main.impl.databinding.ItemCityNameBinding;

import java.util.ArrayList;
import java.util.List;

public class CityListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HOT_HEADER = 0;
    private static final int TYPE_LETTER_HEADER = 2;
    private static final int TYPE_CITY_NAME = 3;

    private final List<Object> allItems = new ArrayList<>();
    private OnCityClickListener cityClickListener;

    public void setData(List<String> hotCities, List<String> letterNav, java.util.Map<String, List<String>> cityDataMap) {
        allItems.clear();
        
        // 添加热门城市区域
        allItems.add(new HotHeader());
        allItems.addAll(hotCities.stream().map(CityItem::new).toList());
        
        // 添加城市列表（按字母分组）
        for (java.util.Map.Entry<String, List<String>> entry : cityDataMap.entrySet()) {
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
        if (item instanceof HotHeader) return TYPE_HOT_HEADER;
        if (item instanceof LetterHeader) return TYPE_LETTER_HEADER;
        return TYPE_CITY_NAME;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HOT_HEADER || viewType == TYPE_LETTER_HEADER) {
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
            ((LetterHeaderViewHolder) holder).bind(item);
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

        void bind(Object item) {
            if (item instanceof HotHeader) {
                binding.tvLetter.setText("推荐城市");
            } else if (item instanceof LetterHeader) {
                binding.tvLetter.setText(((LetterHeader) item).letter);
            }
        }
    }

    class CityNameViewHolder extends RecyclerView.ViewHolder {
        private final ItemCityNameBinding binding;

        CityNameViewHolder(ItemCityNameBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
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

    static class HotHeader {}
    static class LetterHeader {
        String letter;
        LetterHeader(String letter) { this.letter = letter; }
    }
    static class CityItem {
        String name;
        CityItem(String name) { this.name = name; }
    }
}
