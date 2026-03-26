package com.user.profile.data;


import com.agri.pest.client.model.request.ChangePhoneRequest;
import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.agri.pest.client.model.response.ResultString;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.network.NetworkManager;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

public class Repository {
    private UserLocalDataSource localDataSource;
    private UserRemoteDataSource remoteDataSource;

    //做统一管理
    public Repository() {
        localDataSource = new UserLocalDataSource();
        remoteDataSource = new UserRemoteDataSource();
    }

    public Single<ResultUserProfileDto> updateUser(ProfileUpdateRequest update) {
        return remoteDataSource.updateUser(update);
    }

    public void unLogin() {
        localDataSource.unLogin();
    }

    public Single<ResultString> uploadAvatar(MultipartBody.Part part, String fileName) {
        return remoteDataSource.uploadAvatar(part, fileName);
    }

    public Single<ResultUserProfileDto> getUserMes() {
        return remoteDataSource.getUserMes();
    }


    public Single<ResultVoid> updatePassword(String mima) {
        return remoteDataSource.updatePassword(mima);
    }

    public Single<ResultVoid> updatePhone(ChangePhoneRequest request) {
        return remoteDataSource.updatePhone(request);
    }
}
