package com.community.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.agri.pest.client.model.response.CommentResponseDto;
import com.common.utils.ImageLoader;
import com.community.databinding.ItemPostDetailCommentBinding;
import com.uikit.base.BaseBindingAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.TimeZone;

public class PostDetailCommentAdapter extends BaseBindingAdapter<CommentResponseDto, ItemPostDetailCommentBinding> {

    @Override
    public void convert(@NonNull ItemPostDetailCommentBinding binding, @NonNull CommentResponseDto item, int position) {
        binding.tvCommentAuthor.setText(item.getAuthorName());
        binding.tvCommentContent.setText(item.getContent());

        if (item.getCreatedAt() != null) {
            binding.tvCommentDate.setText(formatTimeAgo(item.getCreatedAt().toString()));
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
    private String formatTimeAgo(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date date = sdf.parse(timestamp);
            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long months = days / 30; // 简单近似，实际可根据需求调整

            if (months > 0) return months + "个月前";
            if (days > 0) return days + "天前";
            if (hours > 0) return hours + "小时前";
            if (minutes > 0) return minutes + "分钟前";
            return "刚刚";
        } catch (ParseException e) {
            e.printStackTrace();
            return timestamp;
        }
    }
}
