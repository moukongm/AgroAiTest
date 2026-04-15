package com.main.data;

import com.agri.pest.client.model.request.MyCropCreateRequest;
import com.agri.pest.client.model.response.ResultMyCropResponseDto;
import com.agri.pest.client.model.response.ResultString;
import com.network.NetworkManager;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

public class PlantAddRepository {

    public Single<ResultMyCropResponseDto> createCrop(MyCropCreateRequest request){
        return NetworkManager.INSTANCE.getApi().createCrop(request);
    }

    public Single<ResultString> uploadFile(MultipartBody.Part file){
        return NetworkManager.INSTANCE.getApi().uploadFile(file, null);
    }
}
