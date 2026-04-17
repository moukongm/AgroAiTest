package com.detection.data;

import android.content.Context;

import com.agri.pest.client.model.request.ChatRequest;
import com.agri.pest.client.model.response.ResultChatProfileResponse;
import com.agri.pest.client.model.response.ResultListAgentChatHistory;
import com.agri.pest.client.model.response.ResultListDiagnosisItem;
import com.agri.pest.client.model.response.ResultString;
import com.agri.pest.client.model.response.SseEmitter;
import com.common.storage.database.DetectionRecord;
import com.network.NetworkManager;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

public class Repository {
    private UserLocalDataSource localDataSource;
    private UserRemoteDataSource remoteDataSource;

    public Repository() {
        localDataSource = new UserLocalDataSource();
        remoteDataSource = new UserRemoteDataSource();
    }

    public Single<ResultString> uploadAvatar(MultipartBody.Part part, String fileName) {
        return remoteDataSource.uploadAvatar(part, fileName);
    }

    public Single<ResultListDiagnosisItem> getchat(ChatRequest chatRequest) {
        return remoteDataSource.getchat(chatRequest);
    }

    public Single<ResultListAgentChatHistory> getchatHistory() {
        return remoteDataSource.getchatHistory();
    }
    public void saveToLocalDatabase(String diseaseName, String localImagePath, Context context) {
        localDataSource.saveToLocalDatabase(diseaseName,localImagePath,context);
    }
    public List<DetectionRecord> getLocalRecords(Context context) {
       return localDataSource.getLocalRecords(context);
    }
    public  Single<ResultChatProfileResponse> getAiChat(ChatRequest chatRequest) {

        return remoteDataSource.getAiChat(chatRequest);
    }

    public   Single<SseEmitter> getChatStream(ChatRequest chatRequest) {

        return remoteDataSource.getChatStream(chatRequest);
    }
}
