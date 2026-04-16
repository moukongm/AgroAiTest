package com.main.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;

import com.agri.pest.client.model.response.MessageResponseDto;
import com.common.utils.LogUtils;
import com.main.impl.databinding.ItemNoticeBinding;
import com.uikit.base.BaseBindingAdapter;

public class NoticeAdapter extends BaseBindingAdapter<MessageResponseDto, ItemNoticeBinding> {

    // Item 点击回调接口
    public interface OnNoticeClickListener {
        void onNoticeClick(Long id,int location,Boolean isZk);
    }

    // 回调监听器
    private OnNoticeClickListener listener;

    public NoticeAdapter() {
        super();
    }

    // 设置点击监听器
    public void setOnNoticeClickListener(OnNoticeClickListener listener) {
        this.listener = listener;
    }


    @Override
    public void convert(@NonNull ItemNoticeBinding binding, @NonNull MessageResponseDto item, int position) {
        if("LIKE".equals(item.getType()) ||"COMMENT".equals(item.getType()) ){
            binding.consWarnitem.setVisibility(View.GONE);
            return;
        }
        LogUtils.INSTANCE.d("xjl","yj");
        // 设置预警标题
        String title = item.getTitle() != null ? item.getTitle() : "预警通知";
        binding.mainpageWarningTitle.setText(title);

        // 设置预警内容
        binding.tvWarnning.setText(item.getContent() != null ? item.getContent() : "");

        binding.mainpageWarningRight.setOnClickListener(v -> {
            binding.mainpageWarningRight.setVisibility(View.GONE);
            binding.mainpageWarningGoneright.setVisibility(View.VISIBLE);
            binding.tvWarnning.setMaxLines(100);
            if(!item.isRead()){
                listener.onNoticeClick(item.getId(), position,true);
            }
        });
        binding.mainpageWarningGoneright.setOnClickListener(v -> {
            binding.mainpageWarningRight.setVisibility(View.VISIBLE);
            binding.mainpageWarningGoneright.setVisibility(View.GONE);
            binding.tvWarnning.setMaxLines(2);
            if(!item.isRead()){
                listener.onNoticeClick(item.getId(), position,false);
            }
        });

        // 设置已读状态（小红点）
        Boolean isRead = item.isRead();
        if (isRead != null && isRead) {
            binding.dotThumb.setVisibility(View.GONE);
        } else {
            binding.dotThumb.setVisibility(View.VISIBLE);
        }

        // 根据是否有内容控制显示/隐藏"暂无预警"提示
        if (item.getContent() != null && !item.getContent().isEmpty()) {
            binding.consHaveWarn.setVisibility(View.VISIBLE);
            binding.consNothaveWarn.setVisibility(View.GONE);
        } else {
            binding.consHaveWarn.setVisibility(View.GONE);
            binding.consNothaveWarn.setVisibility(View.VISIBLE);
        }

    }

}
