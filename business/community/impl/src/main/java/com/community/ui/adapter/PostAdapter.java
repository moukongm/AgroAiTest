package com.community.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.agri.pest.client.model.response.PostResponseDto;
import com.common.utils.ImageLoader;
import com.community.R;
import com.community.databinding.ItemCommunityPostBinding;
import com.uikit.base.BaseBindingAdapter;

public class PostAdapter extends BaseBindingAdapter<PostResponseDto, ItemCommunityPostBinding> {

    private OnItemClickListener onItemClickListener;
    private OnLikeClickListener onLikeClickListener;

    public PostAdapter() {
        super();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setOnLikeClickListener(OnLikeClickListener listener) {
        this.onLikeClickListener = listener;
    }

    @Override
    public void convert(@NonNull ItemCommunityPostBinding binding, @NonNull PostResponseDto item, int position) {
        binding.tvTitle.setText(item.getTitle());
        binding.tvUsername.setText(item.getAuthorName());

        Integer likeCount = item.getLikeCount();
        binding.tvLikeCount.setText(String.valueOf(likeCount != null ? likeCount : 0));

        boolean isLiked = item.isLiked() != null && item.isLiked();
        if (isLiked) {
            binding.ivLike.setImageResource(R.drawable.ic_like);
        } else {
            binding.ivLike.setImageResource(R.drawable.ic_unlike);
        }

        if (item.getAuthorAvatar() != null && !item.getAuthorAvatar().isEmpty()) {
            ImageLoader.INSTANCE.loadCircle(binding.ivAvatar, item.getAuthorAvatar());
        }
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            ImageLoader.INSTANCE.load(binding.ivCover, item.getImages().get(0));
        }

        binding.getRoot().setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item, position);
            }
        });

        binding.ivLike.setOnClickListener(v -> {
            if (onLikeClickListener != null) {
                onLikeClickListener.onLikeClick(item, position);
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(PostResponseDto item, int position);
    }

    public interface OnLikeClickListener {
        void onLikeClick(PostResponseDto item, int position);
    }
}
