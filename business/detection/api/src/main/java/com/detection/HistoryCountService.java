package com.detection;

import com.agri.pest.client.model.response.ResultListAgentChatHistory;
import com.alibaba.android.arouter.facade.template.IProvider;
import com.alibaba.android.arouter.launcher.ARouter;

import io.reactivex.rxjava3.core.Single;

/**
 * 历史记录数量服务接口
 */
public interface HistoryCountService extends IProvider {

    /**
     * 获取历史记录总数
     */
    Single<ResultListAgentChatHistory> getHistoryCount();

    static HistoryCountService api() {
        return ARouter.getInstance().navigation(HistoryCountService.class);
    }
}
