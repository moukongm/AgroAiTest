package com.main.data;

import android.content.Context;

import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.agri.pest.client.model.response.ResultMessageGroupResponseDto;
import com.agri.pest.client.model.response.ResultPageResultMessageResponseDto;
import com.agri.pest.client.model.response.ResultPostResponseDto;

import com.agri.pest.client.model.request.MyCropCreateRequest;
import com.agri.pest.client.model.request.MyCropUpdateRequest;
import com.agri.pest.client.model.response.ResultListMyCropResponseDto;
import com.agri.pest.client.model.response.ResultMyCropResponseDto;
import com.agri.pest.client.model.response.ResultString;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.common.utils.LogUtils;
import com.network.LocationRetrofitClient;
import com.network.NetworkManager;
import com.network.model.AlertResponse;
import com.network.model.GeocodeResponse;
import com.network.model.IpLocationResponse;
import com.network.model.SearchCityResponse;
import com.network.model.WeatherResponse;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

import java.time.LocalDate;

public class Repository {
    private UserRemoteDataSource remoteDataSource;

    public Repository() {
        remoteDataSource = new UserRemoteDataSource();
    }

    public Single<ResultUserProfileDto> getUserMes() {

        return remoteDataSource.getUserMes();
    }

    public Single<IpLocationResponse> getLocation(){
        return remoteDataSource.getLocation();

    }

    public Single<GeocodeResponse> getGeoCode(String name){
        return remoteDataSource.getGeoCode(name);
    }

    public Single<WeatherResponse> getWeather(String jwd) {
        LogUtils.INSTANCE.d("ftgbjsdkahkfhad,ukfhkashfa", "repository");
        return remoteDataSource.getWeather(jwd);
    }
    public AMapLocationClient getLocation(Context context) {
        return remoteDataSource.getLocation(context);
    }
    public Single<AlertResponse> getWarning(String jd, String wd) {
        return remoteDataSource.getWarnning(jd, wd);
    }
    public Single<ResultPageResultMessageResponseDto> getWarnningApi() {
        return remoteDataSource.getWarnningApi();
    }

    public  Single<ResultMessageGroupResponseDto> getMessageUser(int i) {
        return remoteDataSource.getMessageUser(i);
    }
    public  Single<ResultVoid> isRead(Long i) {
        return remoteDataSource.isRead(i);
    }
    public  Single<ResultVoid> isReadAll() {
        return remoteDataSource.isReadAll();
    }
    public  Single<ResultPostResponseDto> getPost(Long id) {
        return remoteDataSource.getPost(id);

    }
    public  Single<ResultVoid> ceshi() {
        return Single.error(new UnsupportedOperationException("测试消息发布已禁用"));
    }

    public Single<ResultListMyCropResponseDto> getMyCrops() {
        return remoteDataSource.getMyCrops();
    }

    public Single<ResultVoid> deleteCrop(Long id) {
        return remoteDataSource.deleteCrop(id);
    }

    public Single<ResultMyCropResponseDto> getCropDetail(Long cropId) {
        return remoteDataSource.getCropDetail(cropId);
    }

    public Single<ResultMyCropResponseDto> updateCrop(Long id, MyCropUpdateRequest updateRequest) {
        return remoteDataSource.updateCrop(id, updateRequest);
    }
    public Single<SearchCityResponse> getCity(String jwd) {

        return remoteDataSource.getCity(jwd);
    }
    public Single<ResultVoid> addTag(Long cropId, String tagType, LocalDate recordDate, String content, int status) {
        return remoteDataSource.addTag(cropId, tagType, recordDate, content, status);
    }
    public Single<ResultUserProfileDto> updateLocation(ProfileUpdateRequest jwd) {
        return remoteDataSource.updateLocation(jwd);
    }
    public Single<ResultVoid> cancelTag(Long cropId, String tagType, LocalDate recordDate, int status) {
        return remoteDataSource.cancelTag(cropId, tagType, recordDate, status);
    }

}
