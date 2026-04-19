package com.user.profile.ui.adapters;

import androidx.annotation.NonNull;

import com.uikit.base.BaseBindingAdapter;
import com.user.databinding.ItemGuideRegisterBinding;

import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GuideFirAdapter extends BaseBindingAdapter<String, ItemGuideRegisterBinding> {

    private final Set<String> preselectedItems = new HashSet<>();

    public GuideFirAdapter(int customLayoutResId, @Nullable List<String> data) {
        super(customLayoutResId, data);
    }

    public void setSelectedItems(@Nullable List<String> selectedItems) {
        if (selectedItems != null) {
            preselectedItems.clear();
            preselectedItems.addAll(selectedItems);
            notifyDataSetChanged();
        }
    }

    @Override
    public void convert(@NonNull ItemGuideRegisterBinding binding, String item, int position) {
        binding.chipMelon.setText(item);
        binding.chipMelon.setSelected(preselectedItems.contains(item));
    }
}
