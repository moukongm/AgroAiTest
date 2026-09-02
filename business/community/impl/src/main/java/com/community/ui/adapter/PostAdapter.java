package com.community.ui.adapter;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.agri.pest.client.model.response.ImageSizeDto;
import com.agri.pest.client.model.response.PostResponseDto;
import com.common.utils.LogUtils;
import com.community.R;
import com.community.databinding.ItemCommunityPostBinding;
import com.community.databinding.ItemCommunityPostElderBinding;
import com.uikit.base.BaseBindingAdapter;
import com.uikit.base.BaseBindingViewHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import coil.Coil;
import coil.ImageLoader;
import coil.request.ImageRequest;
import coil.request.ImageResult;
import coil.size.Scale;
import coil.transform.CircleCropTransformation;

public class PostAdapter extends BaseBindingAdapter<PostResponseDto, ItemCommunityPostBinding> {

    private OnItemClickListener onItemClickListener;
    private OnLikeClickListener onLikeClickListener;
    // 缓存图片高度
    private final HashMap<String, Integer> imageHeightCache = new HashMap<>();

    // 默认高度
    private static final int DEFAULT_IMAGE_HEIGHT = 300;

    public static final String PAYLOAD_LIKE_UPDATE = "like_update";

    public PostAdapter() {
        super();
    }

    @Override

    public void onBindViewHolder(BaseBindingViewHolder<ItemCommunityPostBinding> holder,
                                 int position,
                                 List<Object> payloads) {
        PostResponseDto item = getItem(position);
        if(item == null) return;

        if(payloads.isEmpty()) {
            convert((ItemCommunityPostBinding) holder.binding, item, position);
        } else {
            for (Object payload : payloads) {
                if(PAYLOAD_LIKE_UPDATE.equals(payload)) {
                    updateLikeView(holder.binding, item);
                }
            }
        }
    }

    private void updateLikeView(ItemCommunityPostBinding binding, PostResponseDto item) {
        Integer likeCount = item.getLikeCount();
        binding.tvLikeCount.setText(String.valueOf(likeCount != null ? likeCount : 0));
        boolean isLiked = item.isLiked() != null && item.isLiked();
        if (isLiked) {
            binding.ivLike.setImageResource(R.drawable.ic_like);
        } else {
            binding.ivLike.setImageResource(R.drawable.ic_unlike);
        }
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
            binding.ivCover.setVisibility(ImageView.VISIBLE);
            // 防抖：直接用服务端返回的像素高度
            List<ImageSizeDto> imageSizes = item.getImageSizes();
            LogUtils.INSTANCE.d("image",imageSizes.toString());
            if (imageSizes != null && !imageSizes.isEmpty()) {
                ImageSizeDto coverSize = imageSizes.get(0);
                if(coverSize==null){
                    LogUtils.INSTANCE.d("image","null");
                }
                if (coverSize != null && coverSize.getHeight() != null && coverSize.getHeight() > 0) {
                    int coverHeight = coverSize.getHeight();
                    LogUtils.INSTANCE.d("image",coverHeight+"");

                    // 用 ConstraintLayout.LayoutParams 保持约束
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.ivCover.getLayoutParams();
                    params.height = coverHeight;
                    binding.ivCover.setLayoutParams(params);
                }
            }
            ImageLoader imageLoader = Coil.imageLoader(binding.getRoot().getContext());
            ImageRequest coverRequest = new ImageRequest.Builder(binding.getRoot().getContext())
                    .data(item.getImages().get(0))
                    .placeholder(R.drawable.zhanweitu)
                    .error(R.drawable.zhanweitu)
                    .target(binding.ivCover)
                    .scale(Scale.FILL)
                    .build();
            imageLoader.enqueue(coverRequest);
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

    public void updateItemLikeStatus(int position, boolean isLike, int likeCount) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }
        PostResponseDto item = getItem(position);
        if(item == null) {
            return;
        }

        PostResponseDto newItem = new PostResponseDto(
                item.getId(),
                item.getTitle(),
                item.getContent(),
                item.getImages(),
                item.getImageSizes(),
                item.getTags(),
                item.getAuthorId(),
                item.getAuthorName(),
                item.getAuthorUsername(),
                item.getAuthorAvatar(),
                item.getAuthorAvatarWidth(),
                item.getAuthorAvatarHeight(),
                (Integer) likeCount,
                item.getFavoriteCount(),
                item.getCommentCount(),
                isLike,
                item.isFavorited(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        );

        getData().set(position, newItem);

        notifyItemChanged(position, PAYLOAD_LIKE_UPDATE);
    }
    public interface OnItemClickListener {
        void onItemClick(PostResponseDto item, int position);
    }

    public interface OnLikeClickListener {
        void onLikeClick(PostResponseDto item, int position);
    }
}
