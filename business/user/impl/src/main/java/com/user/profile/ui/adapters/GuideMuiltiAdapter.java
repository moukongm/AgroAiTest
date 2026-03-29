package com.user.profile.ui.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.uikit.base.BaseBindingMultiAdapter;
import com.user.databinding.ItemGuideAddBinding;
import com.user.databinding.ItemGuideEditBinding;
import com.user.databinding.ItemGuideRegisterBinding;
import com.user.profile.model.GuideMultiItem;

import org.jetbrains.annotations.NotNull;

public class GuideMuiltiAdapter extends BaseBindingMultiAdapter<GuideMultiItem> {
    public GuideMuiltiAdapter() {
        addItemBinding(GuideMultiItem.TYPE_NEW, (parent, viewType, boo) ->
                ItemGuideRegisterBinding.inflate(LayoutInflater.from(parent.getContext()), viewType, false));
        addItemBinding(GuideMultiItem.TYPE_EDIT, (parent, viewType, boo) ->
                ItemGuideEditBinding.inflate(LayoutInflater.from(parent.getContext()), viewType, false));
        addItemBinding(GuideMultiItem.TYPE_ADD, (parent, viewType, boo) ->
                ItemGuideAddBinding.inflate(LayoutInflater.from(parent.getContext()), viewType, false));
    }

    @Override
    public void convert(@NotNull ViewBinding binding, @NonNull GuideMultiItem item, int itemType, int position) {
        switch (itemType) {
            case GuideMultiItem.TYPE_NEW:
                ItemGuideRegisterBinding binding1 = (ItemGuideRegisterBinding) binding;
                binding1.chipMelon.setText(item.getNewCrop());
                break;
            case GuideMultiItem.TYPE_EDIT:
                ItemGuideEditBinding editBinding = (ItemGuideEditBinding) binding;
                TextWatcher oldWatcher = (TextWatcher) editBinding.etCustom.getTag();
                if (oldWatcher != null) {
                    editBinding.etCustom.removeTextChangedListener(oldWatcher);
                }
                editBinding.etCustom.setText(item.getEditText());

                TextWatcher newWatcher = new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        item.setEditText(s.toString());
                    }
                };
                editBinding.etCustom.addTextChangedListener(newWatcher);
                editBinding.etCustom.setTag(newWatcher);
                break;
        }
    }
}
