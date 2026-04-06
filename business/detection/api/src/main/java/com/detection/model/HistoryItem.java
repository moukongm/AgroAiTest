package com.detection.model;

import androidx.annotation.Nullable;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.chad.library.adapter.base.entity.MultiItemEntity;


public class HistoryItem implements MultiItemEntity {
    public static final int TYPE_DATE = 1;
    public static final int TYPE_ITEM = 2;

    @Nullable
    private final String dateLabel;
    @Nullable
    private final AgentChatHistory chatHistory;
    private final int itemType;

    @Override
    public int getItemType() {
        return itemType;
    }

    public static HistoryItem dateHeader(@Nullable String dateLabel) {
        return new HistoryItem(TYPE_DATE, dateLabel, null);
    }

    public static HistoryItem item(@Nullable AgentChatHistory chatHistory) {
        return new HistoryItem(TYPE_ITEM, null, chatHistory);
    }

    public HistoryItem(int itemType, @Nullable String dateLabel, @Nullable AgentChatHistory chatHistory) {
        this.itemType = itemType;
        this.dateLabel = dateLabel;
        this.chatHistory = chatHistory;
    }

    @Nullable
    public String getDateLabel() {
        return dateLabel;
    }

    @Nullable
    public AgentChatHistory getChatHistory() {
        return chatHistory;
    }
}
