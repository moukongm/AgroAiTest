package com.main.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.main.impl.databinding.ItemCityLetterHeaderBinding;
import com.main.impl.databinding.ItemCityNameBinding;
import com.main.impl.databinding.ItemLocationCardBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CityListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_LOCATION_CARD = 0;
    private static final int TYPE_LETTER_HEADER = 1;
    private static final int TYPE_CITY_NAME = 2;

    private final List<Object> allItems = new ArrayList<>();
    private OnCityClickListener cityClickListener;
    private OnRelocateListener relocateListener;
    private List<String> hotCityList = new ArrayList<>();
    private String currentCity = "";
    private String locatedCity = "";

    public void setData(List<String> letterNav, Map<String, List<String>> cityDataMap, 
                        List<String> hotCities, String currCity, String locCity) {
        allItems.clear();
        this.hotCityList = hotCities != null ? hotCities : new ArrayList<>();
        this.currentCity = currCity != null ? currCity : "";
        this.locatedCity = locCity != null ? locCity : "";
        
        // 添加定位卡片
        allItems.add(new LocationCard());
        
        // 添加城市列表（按字母分组）
        for (Map.Entry<String, List<String>> entry : cityDataMap.entrySet()) {
            allItems.add(new LetterHeader(entry.getKey()));
            for (String city : entry.getValue()) {
                allItems.add(new CityItem(city));
            }
        }
        
        notifyDataSetChanged();
    }
    
    public void updateLocatedCity(String city) {
        this.locatedCity = city != null ? city : "";
        // 只更新定位卡片位置
        if (!allItems.isEmpty() && allItems.get(0) instanceof LocationCard) {
            notifyItemChanged(0);
        }
    }
    
    public void updateCurrentCity(String city) {
        this.currentCity = city != null ? city : "";
        // 只更新定位卡片位置
        if (!allItems.isEmpty() && allItems.get(0) instanceof LocationCard) {
            notifyItemChanged(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object item = allItems.get(position);
        if (item instanceof LocationCard) return TYPE_LOCATION_CARD;
        if (item instanceof LetterHeader) return TYPE_LETTER_HEADER;
        return TYPE_CITY_NAME;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_LOCATION_CARD) {
            ItemLocationCardBinding binding = ItemLocationCardBinding.inflate(inflater, parent, false);
            return new LocationCardViewHolder(binding);
        } else if (viewType == TYPE_LETTER_HEADER) {
            ItemCityLetterHeaderBinding binding = ItemCityLetterHeaderBinding.inflate(inflater, parent, false);
            return new LetterHeaderViewHolder(binding);
        }
        ItemCityNameBinding binding = ItemCityNameBinding.inflate(inflater, parent, false);
        return new CityNameViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = allItems.get(position);
        if (holder instanceof LocationCardViewHolder) {
            ((LocationCardViewHolder) holder).bind();
        } else if (holder instanceof LetterHeaderViewHolder) {
            ((LetterHeaderViewHolder) holder).bind((LetterHeader) item);
        } else if (holder instanceof CityNameViewHolder) {
            ((CityNameViewHolder) holder).bind((CityItem) item);
        }
    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }
    
    /**
     * 获取某个字母第一次出现的位置
     */
    public int getPositionForLetter(String letter) {
        for (int i = 0; i < allItems.size(); i++) {
            Object item = allItems.get(i);
            if (item instanceof LetterHeader && ((LetterHeader) item).letter.equals(letter)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * 根据位置获取对应的字母
     */
    public String getLetterForPosition(int position) {
        if (position < 0 || position >= allItems.size()) return null;
        
        // 从当前位置向上找最近的字母标题
        for (int i = position; i >= 0; i--) {
            Object item = allItems.get(i);
            if (item instanceof LetterHeader) {
                return ((LetterHeader) item).letter;
            }
        }
        return null;
    }

    class LocationCardViewHolder extends RecyclerView.ViewHolder {
        private final ItemLocationCardBinding binding;
        private HotCityRowAdapter hotCityAdapter;

        LocationCardViewHolder(ItemLocationCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            initHotCityRecyclerView();
        }
        
        private void initHotCityRecyclerView() {
            hotCityAdapter = new HotCityRowAdapter();
            binding.rvHotCity.setLayoutManager(new LinearLayoutManager(
                binding.getRoot().getContext(), LinearLayoutManager.VERTICAL, false));
            binding.rvHotCity.setAdapter(hotCityAdapter);
            binding.rvHotCity.setNestedScrollingEnabled(false);
            
            hotCityAdapter.setOnCityClickListener(cityName -> {
                if (cityClickListener != null) {
                    cityClickListener.onCityClick(cityName);
                }
            });
            
            binding.llCurrentLocation.setOnClickListener(v -> {
                if (relocateListener != null) {
                    relocateListener.onRelocate();
                }
            });
        }

        void bind() {
            binding.tvCurrentCity.setText(currentCity);
            binding.tvLocationCity.setText(locatedCity);
            hotCityAdapter.setData(hotCityList);
            hotCityAdapter.setSelectedCity("");
        }
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
    
    public void setOnRelocateListener(OnRelocateListener listener) {
        this.relocateListener = listener;
    }

    public interface OnCityClickListener {
        void onCityClick(String cityName);
    }
    
    public interface OnRelocateListener {
        void onRelocate();
    }

    public static class LocationCard {}

    public static class LetterHeader {
        public String letter;
        public LetterHeader(String letter) { this.letter = letter; }
    }

    public static class CityItem {
        public String name;
        public CityItem(String name) { this.name = name; }
    }
}
