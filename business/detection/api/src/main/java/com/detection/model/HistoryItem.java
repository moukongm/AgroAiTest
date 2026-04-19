package com.detection.model;

import androidx.annotation.Nullable;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.common.storage.database.DetectionRecord;


public class HistoryItem implements MultiItemEntity {
    public static final int TYPE_DATE = 1;
    public static final int TYPE_ITEM = 2;
    public static final int TYPE_LOCAL_ITEM = 3;

    @Nullable
    private final String dateLabel;
    @Nullable
    private final AgentChatHistory chatHistory;
    @Nullable
    private final DetectionRecord localRecord;
    private final int itemType;

    @Override
    public int getItemType() {
        return itemType;
    }

    public static HistoryItem dateHeader(@Nullable String dateLabel) {
        return new HistoryItem(TYPE_DATE, dateLabel, null, null);
    }

    public static HistoryItem item(@Nullable AgentChatHistory chatHistory) {
        return new HistoryItem(TYPE_ITEM, null, chatHistory, null);
    }

    public static HistoryItem localItem(@Nullable DetectionRecord record) {
        return new HistoryItem(TYPE_LOCAL_ITEM, null, null, record);
    }

    public HistoryItem(int itemType, @Nullable String dateLabel, @Nullable AgentChatHistory chatHistory, @Nullable DetectionRecord localRecord) {
        this.itemType = itemType;
        this.dateLabel = dateLabel;
        this.chatHistory = chatHistory;
        this.localRecord = localRecord;
    }

    @Nullable
    public String getDateLabel() {
        return dateLabel;
    }

    @Nullable
    public AgentChatHistory getChatHistory() {
        return chatHistory;
    }

    @Nullable
    public DetectionRecord getLocalRecord() {
        return localRecord;
    }
}
