package com.user.profile.ui.adapters;

import androidx.annotation.NonNull;

import com.common.utils.ImageLoader;
import com.uikit.base.BaseBindingAdapter;
import com.user.databinding.ItemMainpPostavatarBinding;

public class PostAvatarAdapter extends BaseBindingAdapter<String, ItemMainpPostavatarBinding> {

    public PostAvatarAdapter() {
        super(0, null);
    }

    @Override
    public void convert(@NonNull ItemMainpPostavatarBinding binding, String imageUrl, int position) {
        ImageLoader.INSTANCE.load(binding.ivAvatarrecycler, imageUrl);
    }
}
