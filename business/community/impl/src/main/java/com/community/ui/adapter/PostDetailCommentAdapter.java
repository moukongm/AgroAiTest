package com.community.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.agri.pest.client.model.response.CommentResponseDto;
import com.common.utils.ImageLoader;
import com.community.databinding.ItemPostDetailCommentBinding;
import com.uikit.base.BaseBindingAdapter;

public class PostDetailCommentAdapter extends BaseBindingAdapter<CommentResponseDto, ItemPostDetailCommentBinding> {

    @Override
    public void convert(@NonNull ItemPostDetailCommentBinding binding, @NonNull CommentResponseDto item, int position) {
        binding.tvCommentAuthor.setText(item.getAuthorName());
        binding.tvCommentContent.setText(item.getContent());

        if (item.getCreatedAt() != null) {
            binding.tvCommentDate.setText(item.getCreatedAt().toString());
        } else {
            binding.tvCommentDate.setText("");
        }

        String url = item.getAuthorAvatar();
        if (url != null && !url.isEmpty()) {
            ImageLoader.INSTANCE.loadCircle(binding.ivCommentAvatar, url);
        } else {
            binding.ivCommentAvatar.setImageDrawable(null);
        }

        binding.tvCommentReply.setOnClickListener(v -> {

        });
    }
}
