package com.detection.impl;

import android.content.Context;

import com.agri.pest.client.model.response.ResultListAgentChatHistory;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.router.RouterPath;
import com.detection.DetectionService;
import com.detection.HistoryCountService;
import com.detection.viewmodel.DetectionViewModel;

import io.reactivex.rxjava3.core.Single;

@Route(path = RouterPath.HISTORY_COUNT_SERVICE)
public class HistoryCountServiceImpl implements HistoryCountService {
    DetectionViewModel detectionViewModel;

    @Override
    public void init(Context context) {
        // 初始化逻辑
        detectionViewModel = new DetectionViewModel();
    }

    @Override
    public Single<ResultListAgentChatHistory> getHistoryCount() {

        return detectionViewModel.getHistoryCount();
    }
}
