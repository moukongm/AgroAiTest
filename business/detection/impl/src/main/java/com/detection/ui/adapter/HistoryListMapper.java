package com.detection.ui.adapter;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.common.storage.database.DetectionRecord;
import com.common.utils.LogUtils;
import com.detection.model.HistoryItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
                    LogUtils.INSTANCE.d("ljxjxl",i+"");
                    result.add(HistoryItem.dateHeader(time.isEmpty() ? "--" : time));
                    result.add(HistoryItem.dateHeader(""));
                    i+=2;
                }
                else{
                    LogUtils.INSTANCE.d("ljxjxl",i+"");
                    result.add(HistoryItem.dateHeader(""));
                    result.add(HistoryItem.dateHeader(time.isEmpty() ? "--" : time));
                    result.add(HistoryItem.dateHeader(""));
                    i+=3;
                }
                lastTime = time;
            }
            result.add(HistoryItem.item(chatHistory));
            i++;
        }
        return result;
    }

    
    public static List<HistoryItem> toMultiListFromLocal(List<DetectionRecord> records) {
        List<HistoryItem> result = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return result;
        }

        // 按时间倒序排列
        records.sort((a, b) -> Long.compare(b.getRecognitionTime(), a.getRecognitionTime()));

        String lastTime = null;
        int i = 0;
        for (DetectionRecord record : records) {
            String time = extractDateKeyFromLocal(record);
            if (!time.equals(lastTime)) {
                if(i % 2== 0){
                    LogUtils.INSTANCE.d("ljxjxl",i+"");
                    result.add(HistoryItem.dateHeader(time.isEmpty() ? "--" : time));
                    result.add(HistoryItem.dateHeader(""));
                    i+=2;
                }
                else{
                    LogUtils.INSTANCE.d("ljxjxl",i+"");
                    result.add(HistoryItem.dateHeader(""));
                    result.add(HistoryItem.dateHeader(time.isEmpty() ? "--" : time));
                    result.add(HistoryItem.dateHeader(""));
                    i+=3;
                }
                lastTime = time;
            }
            else{
                LogUtils.INSTANCE.d("ljxjxl",i+"");
            }
            i++;
            result.add(HistoryItem.localItem(record));
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

    private static String extractDateKeyFromLocal(DetectionRecord record) {
        long timestamp = record.getRecognitionTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
