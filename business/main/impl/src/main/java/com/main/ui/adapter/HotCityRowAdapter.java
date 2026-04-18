package com.main.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.main.impl.R;
import com.main.impl.databinding.ItemCityHotRowBinding;

import java.util.ArrayList;
import java.util.List;

public class HotCityRowAdapter extends RecyclerView.Adapter<HotCityRowAdapter.ViewHolder> {

    private List<List<String>> rows = new ArrayList<>();
    private OnCityClickListener listener;
    private String selectedCity = "";

    public void setSelectedCity(String city) {
        if (!selectedCity.equals(city)) {
            selectedCity = city;
            notifyDataSetChanged();
        }
    }

    public void setData(List<String> hotCities) {
        rows.clear();
        // 按4列分组成多行
        for (int i = 0; i < hotCities.size(); i += 4) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < 4 && i + j < hotCities.size(); j++) {
                row.add(hotCities.get(i + j));
            }
            rows.add(row);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCityHotRowBinding binding = ItemCityHotRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(rows.get(position));
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemCityHotRowBinding binding;

        ViewHolder(ItemCityHotRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(List<String> row) {
            binding.tvCity1.setVisibility(View.GONE);
            binding.tvCity2.setVisibility(View.GONE);
            binding.tvCity3.setVisibility(View.GONE);
            binding.tvCity4.setVisibility(View.GONE);

            if (row.size() >= 1) {
                binding.tvCity1.setText(row.get(0));
                binding.tvCity1.setVisibility(View.VISIBLE);
                boolean isSelected1 = row.get(0).equals(selectedCity);
                binding.tvCity1.setSelected(isSelected1);
                binding.tvCity1.setTextColor(isSelected1 ? 0xFFFFFFFF : 0xFF333333);
                binding.tvCity1.setOnClickListener(v -> {
                    notifyClick(row.get(0));
                    setSelectedCity(row.get(0));
                });
            }
            if (row.size() >= 2) {
                binding.tvCity2.setText(row.get(1));
                binding.tvCity2.setVisibility(View.VISIBLE);
                boolean isSelected2 = row.get(1).equals(selectedCity);
                binding.tvCity2.setSelected(isSelected2);
                binding.tvCity2.setTextColor(isSelected2 ? 0xFFFFFFFF : 0xFF333333);
                binding.tvCity2.setOnClickListener(v -> {
                    notifyClick(row.get(1));
                    setSelectedCity(row.get(1));
                });
            }
            if (row.size() >= 3) {
                binding.tvCity3.setText(row.get(2));
                binding.tvCity3.setVisibility(View.VISIBLE);
                boolean isSelected3 = row.get(2).equals(selectedCity);
                binding.tvCity3.setSelected(isSelected3);
                binding.tvCity3.setTextColor(isSelected3 ? 0xFFFFFFFF : 0xFF333333);
                binding.tvCity3.setOnClickListener(v -> {
                    notifyClick(row.get(2));
                    setSelectedCity(row.get(2));
                });
            }
            if (row.size() >= 4) {
                binding.tvCity4.setText(row.get(3));
                binding.tvCity4.setVisibility(View.VISIBLE);
                boolean isSelected4 = row.get(3).equals(selectedCity);
                binding.tvCity4.setSelected(isSelected4);
                binding.tvCity4.setTextColor(isSelected4 ? 0xFFFFFFFF : 0xFF333333);
                binding.tvCity4.setOnClickListener(v -> {
                    notifyClick(row.get(3));
                    setSelectedCity(row.get(3));
                });
            }
        }

        private void notifyClick(String cityName) {
            if (listener != null) {
                listener.onCityClick(cityName);
            }
        }
    }

    public void setOnCityClickListener(OnCityClickListener listener) {
        this.listener = listener;
    }

    public interface OnCityClickListener {
        void onCityClick(String cityName);
    }
}
