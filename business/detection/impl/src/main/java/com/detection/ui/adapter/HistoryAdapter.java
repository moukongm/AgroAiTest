package com.detection.ui.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.common.utils.ImageLoader;
import com.detection.R;
import com.detection.Utils;
import com.detection.databinding.ItemHistoryRv1DateBinding;
import com.detection.databinding.ItemHistoryRv2ItemBinding;
import com.detection.model.HistoryItem;
import com.uikit.base.BaseBindingMultiAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryAdapter extends BaseBindingMultiAdapter<HistoryItem> {

    public HistoryAdapter() {
        super(null);
        addItemBinding(HistoryItem.TYPE_DATE, (inflater, parent, attach) ->
                ItemHistoryRv1DateBinding.inflate(inflater, parent, false));
        addItemBinding(HistoryItem.TYPE_ITEM, (inflater, parent, attach) ->
                ItemHistoryRv2ItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void convert(@NotNull ViewBinding binding, @NonNull HistoryItem item, int itemType, int position) {
        if (itemType == HistoryItem.TYPE_DATE) {
            ItemHistoryRv1DateBinding b = (ItemHistoryRv1DateBinding) binding;
            b.tvStarDate.setText(item.getDateLabel() != null ? item.getDateLabel() : "");
            return;
        }
        if (itemType == HistoryItem.TYPE_ITEM) {
            ItemHistoryRv2ItemBinding b = (ItemHistoryRv2ItemBinding) binding;
            AgentChatHistory chatHistory = item.getChatHistory();
            if (chatHistory == null) {
                return;
            }
            // 封面图
            String image = chatHistory.getImageUrl();
            if (image != null && !image.isEmpty()) {
                ImageLoader.INSTANCE.load(b.ivStarPostCover, image);
            } else {
                b.ivStarPostCover.setImageResource(R.drawable.im_history_shili);
            }
            String displayText = Utils.extractDiseaseName(chatHistory.getAgentResponse());
            b.tvStarPostDesc.setText(displayText);
        }
    }
}
