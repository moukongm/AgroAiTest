package com.user.profile.ui.adapters;

import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;

import com.agri.pest.client.model.response.PostResponseDto;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.common.utils.ImageLoader;
import com.common.utils.LogUtils;
import com.uikit.base.BaseBindingAdapter;
import com.user.databinding.ItemMainpPostBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class MinePostAdapter extends BaseBindingAdapter<PostResponseDto, ItemMainpPostBinding> {

    private OnImageClickListener imageClickListener;
    public interface OnImageClickListener {
        void onImageClick(Long id);
    }
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.imageClickListener = listener;
    }

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

        LogUtils.INSTANCE.d("ljxandxzr",item.getAuthorName());
        // 关注作物
        if (item.getTags() != null && !item.getTags().isEmpty()) {
            StringBuilder res =new StringBuilder();
            for (Object o : item.getTags().toArray()) {
                res.append(o).append("、");
            }
            binding.tvPostCrops.setText("关注作物：" + res.deleteCharAt(res.length() - 1));
            binding.tvPostCrops.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.tvPostCrops.setVisibility(android.view.View.GONE);
        }

        // 帖子图片列表
        PostAvatarAdapter avatarAdapter = new PostAvatarAdapter();
        if (binding.recycler.getLayoutManager() == null) {
            binding.recycler.setLayoutManager(new LinearLayoutManager(binding.getRoot().getContext(), LinearLayoutManager.HORIZONTAL, false));

        }

        avatarAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (imageClickListener != null) {
                    imageClickListener.onImageClick(item.getId());
                }
            }
        });
        binding.recycler.setAdapter(avatarAdapter);
        List<String> imageUrls = item.getImages();
        LogUtils.INSTANCE.d("ljxandxzr",item.getId() +"");
        if (imageUrls != null && !imageUrls.isEmpty()) {
            avatarAdapter.setList(imageUrls);
        }

        binding.tvPostContent.setText(item.getContent() != null ? item.getContent() : "");

        int favCount = item.getFavoriteCount();
        binding.tvPostSaveCount.setText(favCount > 0 ? "+" + favCount + "收藏" : "0收藏");
        if(favCount == 0){
            binding.ivSaveAvatar1.setVisibility(View.GONE);
            binding.ivSaveAvatar2.setVisibility(View.GONE);
        } else if (favCount == 1) {
            //后端没给返回的字段；
//            ImageLoader.INSTANCE.loadCircle(binding.ivSaveAvatar1,);
            binding.ivSaveAvatar2.setVisibility(View.GONE);
        }
        else{
            binding.ivSaveAvatar1.setVisibility(View.VISIBLE);
            binding.ivSaveAvatar2.setVisibility(View.VISIBLE);
        }

        binding.tvPostTime.setText(formatTimeAgo(item.getCreatedAt().toString()));
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
