package com.detection.ui.adapter;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import com.agri.pest.client.model.response.AgentChatHistory;
import com.agri.pest.client.model.response.DiagnosisItem;
import com.common.storage.database.DetectionRecord;
import com.common.utils.FileUtils;
import com.common.utils.ImageLoader;
import com.common.utils.LogUtils;
import com.detection.R;
import com.detection.databinding.ItemHistoryRv1DateBinding;
import com.detection.databinding.ItemHistoryRv2ItemBinding;
import com.detection.model.HistoryItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.uikit.base.BaseBindingMultiAdapter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends BaseBindingMultiAdapter<HistoryItem> {

    public HistoryAdapter() {
        super(null);
        addItemBinding(HistoryItem.TYPE_DATE, (inflater, parent, attach) ->
                ItemHistoryRv1DateBinding.inflate(inflater, parent, false));
        addItemBinding(HistoryItem.TYPE_ITEM, (inflater, parent, attach) ->
                ItemHistoryRv2ItemBinding.inflate(inflater, parent, false));
        addItemBinding(HistoryItem.TYPE_LOCAL_ITEM, (inflater, parent, attach) ->
                ItemHistoryRv2ItemBinding.inflate(inflater, parent, false));
    }

    @Override
    public void convert(@NotNull ViewBinding binding, @NonNull HistoryItem item, int itemType, int position) {
        if (itemType == HistoryItem.TYPE_DATE) {
            ItemHistoryRv1DateBinding b = (ItemHistoryRv1DateBinding) binding;
            b.tvStarDate.setText(item.getDateLabel() != null ? item.getDateLabel() : "");
            return;
        }
        if (itemType == HistoryItem.TYPE_ITEM || itemType == HistoryItem.TYPE_LOCAL_ITEM) {
            ItemHistoryRv2ItemBinding b = (ItemHistoryRv2ItemBinding) binding;
            LogUtils.INSTANCE.d("opopop","qwertyuiop");
            if (itemType == HistoryItem.TYPE_LOCAL_ITEM) {
                // 本地记录
                DetectionRecord localRecord = item.getLocalRecord();
                if (localRecord == null) {
                    return;
                }
                // 使用本地图片路径
                String localPath = localRecord.getImageUrl();
                if (localPath != null && !localPath.isEmpty()) {
                    File localFile = new File(localPath);
                    if (localFile.exists()) {
                        b.ivStarPostCover.setImageURI(android.net.Uri.fromFile(localFile));
                    } else {
                        b.ivStarPostCover.setImageResource(R.drawable.im_history_shili);
                    }
                } else {
                    b.ivStarPostCover.setImageResource(R.drawable.im_history_shili);
                }
                b.tvStarPostDesc.setText(localRecord.getDiseaseName() != null ? localRecord.getDiseaseName() : "无法识别");
            } else {
                // 网络记录
                AgentChatHistory chatHistory = item.getChatHistory();
                if (chatHistory == null) {
                    return;
                }
                // 封面图
                String image = chatHistory.getImageUrl();
                if (image != null && !image.isEmpty()) {
                    ImageLoader.INSTANCE.load(b.ivStarPostCover, image, R.drawable.bg_cover_loading, R.drawable.im_history_shili);
                } else {
                    b.ivStarPostCover.setImageResource(R.drawable.im_history_shili);
                }
                Gson gson = new Gson();
                Type listType = new TypeToken<List<DiagnosisItem>>(){}.getType();
                List<DiagnosisItem> result;
                try {
                    result = gson.fromJson(chatHistory.getAgentResponse(), listType);
                } catch (JsonSyntaxException e) {
                   result = new ArrayList<>();
                }
                if (result == null || result.isEmpty()) {
                    b.tvStarPostDesc.setText("无法识别");
                }else{
                    b.tvStarPostDesc.setText(result.get(0).getDiseaseName());
                }
            }
        }
    }
}
