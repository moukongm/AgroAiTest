package com.detection.ui.adapter;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.common.utils.ImageLoader;
import com.detection.databinding.ItemChatAiBinding;
import com.detection.databinding.ItemChatUserBinding;
import com.detection.databinding.ItemHistoryRv1DateBinding;
import com.detection.databinding.ItemHistoryRv2ItemBinding;
import com.detection.model.ChatItem;
import com.detection.model.HistoryItem;
import com.uikit.base.BaseBindingMultiAdapter;

import org.jetbrains.annotations.NotNull;

import io.noties.markwon.Markwon;

public class ChatAdapter extends BaseBindingMultiAdapter<ChatItem> {

    public ChatAdapter() {
        super(null);
        addItemBinding(ChatItem.TYPE_USER, (inflater, parent, attach) ->
                ItemChatUserBinding.inflate(inflater, parent, false));
        addItemBinding(ChatItem.TYPE_AI, (inflater, parent, attach) ->
                ItemChatAiBinding.inflate(inflater, parent, false));
    }

    @Override
    public void convert(@NotNull ViewBinding binding, @NonNull ChatItem item, int itemType, int position) {
        Markwon markwon = Markwon.builder(binding.getRoot().getContext()).build();
        if(itemType==ChatItem.TYPE_AI){
            ItemChatAiBinding b = (ItemChatAiBinding) binding;
            markwon.setMarkdown(b.tvLeft,item.content);
        }
        if(itemType == ChatItem.TYPE_USER){
            ItemChatUserBinding b = (ItemChatUserBinding) binding;
            if(item.imageUrl != null && !item.imageUrl.isEmpty()){
                b.ivUserimg.setVisibility(View.VISIBLE);
                ImageLoader.INSTANCE.load(b.ivUserimg,item.imageUrl);
            }
            else{
                b.ivUserimg.setVisibility(View.GONE);
            }
            if(item.content.isEmpty()){
                b.tvRight.setVisibility(View.GONE);
            }
            else{
                b.tvRight.setVisibility(View.VISIBLE);
                b.tvRight.setText(item.content);
            }
        }
    }
}
