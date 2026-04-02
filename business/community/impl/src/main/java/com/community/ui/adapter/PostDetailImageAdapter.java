package com.community.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.common.utils.ImageLoader;
import com.community.R;
import com.community.databinding.ItemPostDetailImageBinding;
import com.uikit.base.BaseBindingAdapter;

import java.util.ArrayList;
import java.util.List;

public class PostDetailImageAdapter extends BaseBindingAdapter<String, ItemPostDetailImageBinding> {

    public PostDetailImageAdapter() {
        super();
    }

    public void convert(@NonNull ItemPostDetailImageBinding binding, @NonNull String imageUrl, int position) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageLoader.INSTANCE.load(binding.ivImage, imageUrl);
        }
    }
}
