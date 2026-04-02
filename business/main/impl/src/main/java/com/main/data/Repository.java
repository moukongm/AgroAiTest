package com.main.data;

import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.network.LocationRetrofitClient;
import com.network.model.AlertResponse;
import com.network.model.GeocodeResponse;
import com.network.model.IpLocationResponse;
import com.network.model.WeatherResponse;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

public class Repository {
    private UserLocalDataSource localDataSource;
    private UserRemoteDataSource remoteDataSource;

    public Repository() {
        localDataSource = new UserLocalDataSource();
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
        return remoteDataSource.getWeather(jwd);
    }

    public Single<AlertResponse> getWarning(String jd, String wd) {
        return remoteDataSource.getWarnning(jd, wd);
    }

}
