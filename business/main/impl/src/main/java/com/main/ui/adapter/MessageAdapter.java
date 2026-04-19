package com.main.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.agri.pest.client.model.response.MessageResponseDto;
import com.common.utils.DateUtils;
import com.common.utils.ImageLoader;
import com.main.MessageCommentResponseDto;
import com.main.impl.R;
import com.main.impl.databinding.ItemMessageBinding;
import com.main.impl.databinding.ItemMessageElderBinding;
import com.uikit.base.BaseBindingAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MessageAdapter extends BaseBindingAdapter<MessageCommentResponseDto, ItemMessageBinding> {

    public MessageAdapter() {
        super();
    }


    @Override
    public void convert(@NonNull ItemMessageBinding binding, @NonNull MessageCommentResponseDto item, int position) {
        // 设置标题（用户昵称/系统名称）
        binding.tvTitle.setText(item.getMessageResponseDto().getSenderName() != null ? item.getMessageResponseDto().getSenderName() : "未知用户");

       if( "LIKE".equals(item.getMessageResponseDto().getType()) ||"COMMENT".equals(item.getMessageResponseDto().getType())){
           if("LIKE".equals(item.getMessageResponseDto().getType())){
               binding.tvSubtitle.setText("点赞了你的帖子");
           }
           else{
               binding.tvSubtitle.setText("评论了你的帖子");
           }
       }
        OffsetDateTime createdAt = item.getMessageResponseDto().getCreatedAt();
        // 设置时间（转换为友好格式）
        String timeText = formatTimeAgo(createdAt.toString());
        binding.tvTime.setText(timeText);

        // 设置头像
        if (item.getMessageResponseDto().getSenderAvatar() != null && !item.getMessageResponseDto().getSenderAvatar().isEmpty()) {
            ImageLoader.INSTANCE.loadCircle(binding.ivPlavatar, item.getMessageResponseDto().getSenderAvatar());
            binding.consAvater.setBackgroundColor(R.color.transpart);
            binding.ivAvatar.setVisibility(View.INVISIBLE);
        }

        // 设置已读状态（小圆点）
        Boolean isRead = item.getMessageResponseDto().isRead();
        if (isRead != null && isRead) {
            binding.dotThumb.setVisibility(View.GONE);
        } else {
            binding.dotThumb.setVisibility(View.VISIBLE);
        }

        if(item.getUrl() != null && !item.getUrl().isEmpty()){
            binding.ivThumb.setVisibility(View.VISIBLE);
            ImageLoader.INSTANCE.load(binding.ivThumb,item.getUrl());
        }else{
            binding.ivThumb.setVisibility(View.GONE);
        }

        // 隐藏右侧缩略图（消息列表不需要显示）
//        binding.ivThumb.setVisibility(View.GONE);

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
