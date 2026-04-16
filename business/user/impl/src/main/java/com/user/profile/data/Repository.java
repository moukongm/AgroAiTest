package com.user.profile.data;


import android.content.Context;

import com.agri.pest.client.model.request.ChangePhoneRequest;
import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.agri.pest.client.model.response.ResultPageResultPostResponseDto;
import com.agri.pest.client.model.response.ResultString;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.network.NetworkManager;
import com.user.login.data.UserStorageConstant;
import com.common.storage.database.UserRecord;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;

public class Repository {
    private UserLocalDataSource localDataSource;
    private UserRemoteDataSource remoteDataSource;

    public Repository(Context context) {
        localDataSource = new UserLocalDataSource(context);
        remoteDataSource = new UserRemoteDataSource();
    }

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

    public Single<ResultPageResultPostResponseDto> getFavoritesPosts(int page){
        return remoteDataSource.getFavoritesPosts(page);
    }

    public Single<ResultVoid> updatePassword(String mima) {
        return remoteDataSource.updatePassword(mima);
    }

    public Single<ResultVoid> updatePhone(ChangePhoneRequest request) {
        return remoteDataSource.updatePhone(request);
    }

    public Single<ResultPageResultPostResponseDto> getMinePosts(int page){
        return remoteDataSource.getMinePosts(page);
    }

    public void updateLocalTele(String newTele) {
        localDataSource.updateTele(newTele);
    }
    public void updateLocalNickname(String newNickname) {
        localDataSource.updateNickname(newNickname);

    }
    public void updateLocalPassword(String newPassword) {
        localDataSource.updatePassword(newPassword);
    }
    public void updateLocalAvatar(String avatarLocalPath) {
        localDataSource.updateAvatarLocalPath(avatarLocalPath);
    }

    public UserRecord getLocalUser() {
        return localDataSource.getLocalUser();
    }

    public int getLocalDetectionCount() {
        return localDataSource.getLocalDetectionCount();
    }

    public String getLocalNickname() {
        return localDataSource.getLocalNickname();
    }

}
