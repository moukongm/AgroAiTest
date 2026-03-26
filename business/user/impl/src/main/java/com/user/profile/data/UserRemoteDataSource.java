package com.user.profile.data;

import com.agri.pest.client.model.request.ChangePhoneRequest;
import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.agri.pest.client.model.response.ResultString;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.common.utils.LogUtils;
import com.network.NetworkManager;


import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

public class UserRemoteDataSource {
//    private final ApiService api = NetworkManager.INSTANCE.getApi();

    public Single<ResultUserProfileDto> updateUser(ProfileUpdateRequest profileUpdateRequest) {
        //写定义好的接口

        return NetworkManager.INSTANCE.getApi().updateProfile(profileUpdateRequest);
    }

    public Single<ResultString> uploadAvatar(MultipartBody.Part part, String fileName) {
        //写定义好的接口
        return NetworkManager.INSTANCE.getApi().uploadFile(part, fileName);
    }

    public Single<ResultUserProfileDto> getUserMes() {

        return NetworkManager.INSTANCE.getApi().getCurrentUserProfile();
    }

    public Single<ResultVoid> updatePassword(String mima) {
        return NetworkManager.INSTANCE.getApi().updatePassword(mima);
    }

    public Single<ResultVoid> updatePhone(ChangePhoneRequest request) {
        LogUtils.INSTANCE.d("ljxphone", "phone");
        return NetworkManager.INSTANCE.getApi().changePhone(request);
    }


}