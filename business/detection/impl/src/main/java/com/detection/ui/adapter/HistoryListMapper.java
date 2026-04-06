package com.detection.ui.adapter;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.detection.model.HistoryItem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HistoryListMapper {

    private HistoryListMapper() {
    }

    public static List<HistoryItem> toMultiList(List<AgentChatHistory> chatHistories) {
        List<HistoryItem> result = new ArrayList<>();
        if (chatHistories == null || chatHistories.isEmpty()) {
            return result;
        }

        String lastTime = null;
        int i = 0;
        for (AgentChatHistory chatHistory : chatHistories) {
            String time = extractDateKey(chatHistory);
            if (!time.equals(lastTime)) {
                if(i % 2== 0){
                    result.add(HistoryItem.dateHeader(time.isEmpty() ? "--" : time));
                    result.add(HistoryItem.dateHeader(""));
                }
                else{
                    result.add(HistoryItem.dateHeader(""));
                    result.add(HistoryItem.dateHeader(time.isEmpty() ? "--" : time));
                    result.add(HistoryItem.dateHeader(""));
                }
                lastTime = time;
            }
            i++;
            result.add(HistoryItem.item(chatHistory));
        }
        return result;
    }

    private static String extractDateKey(AgentChatHistory p) {
        String s = p.getCreateTime().toString();
        if (s.length() >= 10) {
            return s.substring(0, 10);
        }
        return "";
    }
}
