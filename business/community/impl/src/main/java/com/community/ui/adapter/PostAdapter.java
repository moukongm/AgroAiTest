package com.community.ui.adapter;

import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.agri.pest.client.model.response.PostResponseDto;
import com.community.R;
import com.community.databinding.ItemCommunityPostBinding;
import com.community.databinding.ItemCommunityPostElderBinding;
import com.uikit.base.BaseBindingAdapter;

import coil.Coil;
import coil.ImageLoader;
import coil.request.ImageRequest;
import coil.size.Scale;
import coil.transform.CircleCropTransformation;

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
            ImageLoader imageLoader = Coil.imageLoader(binding.getRoot().getContext());
            ImageRequest avatarRequest = new ImageRequest.Builder(binding.getRoot().getContext())
                    .data(item.getAuthorAvatar())
                    .placeholder(R.drawable.bg_community_post_avatar)
                    .error(R.drawable.bg_community_post_avatar)
                    .target(binding.ivAvatar)
                    .transformations(new CircleCropTransformation())
                    .build();
            imageLoader.enqueue(avatarRequest);
        } else {
            binding.ivAvatar.setImageResource(R.drawable.bg_community_post_avatar);
        }

        if (item.getImages() != null && !item.getImages().isEmpty()) {
            binding.ivCover.setImageDrawable(null);

            ImageLoader imageLoader = Coil.imageLoader(binding.getRoot().getContext());
            ImageRequest coverRequest = new ImageRequest.Builder(binding.getRoot().getContext())
                    .data(item.getImages().get(0))
                    .placeholder(R.drawable.zhanweitu)
                    .error(R.drawable.zhanweitu)
                    .target(binding.ivCover)
                    .scale(Scale.FILL)
                    .build();
            imageLoader.enqueue(coverRequest);
            binding.ivCover.setVisibility(ImageView.VISIBLE);
        } else {
            binding.ivCover.setVisibility(ImageView.GONE);
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
