package com.detection.data;

import android.content.Context;
import android.util.Log;

import com.agri.pest.client.model.request.ChatRequest;
import com.agri.pest.client.model.response.AgentChatHistory;
import com.agri.pest.client.model.response.ResultListAgentChatHistory;
import com.agri.pest.client.model.response.ResultListDiagnosisItem;
import com.agri.pest.client.model.response.ResultString;
import com.common.utils.LogUtils;
import com.network.NetworkManager;
import com.user.TokenService;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

public class UserRemoteDataSource {
    public Single<ResultString> uploadAvatar(MultipartBody.Part part, String fileName) {
        return NetworkManager.INSTANCE.getApi().uploadFile(part, fileName)
                .retryWhen(errors -> errors
                        .flatMap(error -> {
                            if (isTokenExpired(error)) {
                                return refreshTokenAndRetry();
                            }
                            return Flowable.error(error);
                        }));
    }
    public  Single<ResultListDiagnosisItem> getchat(ChatRequest chatRequest) {

        return NetworkManager.INSTANCE.getApi().chat(chatRequest)
                .retryWhen(errors -> errors
                .flatMap(error -> {
                    if (isTokenExpired(error)) {
                        return refreshTokenAndRetry();
                    }
                    return Flowable.error(error);
                }));
    }
    public Single<ResultListAgentChatHistory> getchatHistory() {

        return NetworkManager.INSTANCE.getApi().getHistory()
                .retryWhen(errors -> errors
                        .flatMap(error -> {
                            if (isTokenExpired(error)) {
                                return refreshTokenAndRetry();
                            }
                            return Flowable.error(error);
                        }));
    }
    private boolean isTokenExpired(Throwable error) {
        if (error instanceof retrofit2.HttpException) {
            retrofit2.HttpException httpError = (retrofit2.HttpException) error;
            if (httpError.code() == 401) {
                return true;
            }
        }
        return false;
    }

    private Flowable<?> refreshTokenAndRetry() {
        if (TokenService.api().refreshToken()) {
            LogUtils.INSTANCE.d("TokenRefresh", "token刷新成功");
            return Flowable.empty();
        } else {
            return Flowable.error(new Exception("刷新token失败"));
        }
    }
}
