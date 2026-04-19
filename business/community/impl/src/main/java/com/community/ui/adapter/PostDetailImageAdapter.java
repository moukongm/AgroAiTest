package com.community.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import coil.Coil;
import coil.request.ImageRequest;
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
            ImageRequest request = new ImageRequest.Builder(binding.getRoot().getContext())
                    .data(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .target(binding.ivImage)
                    .build();
            Coil.imageLoader(binding.getRoot().getContext()).enqueue(request);
        }
    }
}
