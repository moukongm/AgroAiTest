package com.user.profile.ui.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.agri.pest.client.model.response.PostResponseDto;
import com.common.utils.ImageLoader;
import com.uikit.base.BaseBindingAdapter;
import com.user.databinding.ItemMainpPostBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MinePostAdapter extends BaseBindingAdapter<PostResponseDto, ItemMainpPostBinding> {

    public MinePostAdapter() {
        super(0, null);
    }

    @Override
    public void convert(@NonNull ItemMainpPostBinding binding, PostResponseDto item, int position) {
        // 用户头像
        if (item.getAuthorAvatar()!= null && !item.getAuthorAvatar().isEmpty()) {
            ImageLoader.INSTANCE.loadCircle(binding.ivPostAvatar, item.getAuthorAvatar());
        }

        // 用户名
        binding.tvPostUsername.setText(item.getAuthorName() != null ? item.getAuthorName() : "");

        // 关注作物
        if (item.getTags() != null && !item.getTags().isEmpty()) {
            binding.tvPostCrops.setText("关注作物：" + item.getTags());
            binding.tvPostCrops.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.tvPostCrops.setVisibility(android.view.View.GONE);
        }

        // 帖子图片列表
        PostAvatarAdapter avatarAdapter = new PostAvatarAdapter();
        binding.recycler.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recycler.setAdapter(avatarAdapter);
        List<String> imageUrls = item.getImages();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            avatarAdapter.setList(imageUrls);
        }

        binding.tvPostContent.setText(item.getContent() != null ? item.getContent() : "");

        int favCount = item.getFavoriteCount();
        binding.tvPostSaveCount.setText(favCount > 0 ? "+" + favCount + "收藏" : "0收藏");

        binding.tvPostTime.setText(formatTimeAgo(item.getCreatedAt().toString()));
    }

    private String formatTimeAgo(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(timestamp);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int days = cal.get(Calendar.DAY_OF_MONTH);
        int hours = cal.get(Calendar.HOUR_OF_DAY);
        int minutes = cal.get(Calendar.MINUTE);
        if (days > 30) {
            return (days / 30) + "个月前";
        } else if (days > 0) {
            return days + "天前";
        } else if (hours > 0) {
            return hours + "小时前";
        } else if (minutes > 0) {
            return minutes + "分钟前";
        } else {
            return "刚刚";
        }
    }
}
