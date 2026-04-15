package com.main.data;

import android.content.Context;
import android.util.Log;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.AdminMessageCreateRequest;
import com.agri.pest.client.model.response.AuthResponse;
import com.agri.pest.client.model.response.ResultMessageGroupResponseDto;
import com.agri.pest.client.model.response.ResultPageResultMessageResponseDto;
import com.agri.pest.client.model.response.ResultPostResponseDto;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.common.utils.LogUtils;
import com.network.LocationRetrofitClient;
import com.network.NetworkManager;
import com.network.model.AlertResponse;
import com.network.model.GeocodeResponse;
import com.network.model.IpLocationResponse;
import com.network.model.WeatherResponse;
import com.user.TokenService;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class UserRemoteDataSource {

    private static final String authorization = "4215780fd9cd4251aee60bfd83d565c2";

    public Single<ResultUserProfileDto> getUserMes() {
        return NetworkManager.INSTANCE.getApi().getCurrentUserProfile()
                .retryWhen(errors -> errors
                        .flatMap(error -> {
                            if (isTokenExpired(error)) {
                                return refreshTokenAndRetry();
                            }
                            return Flowable.error(error);
                        }));
    }
    public AMapLocationClient  getLocation(Context context) {

        try {
            AMapLocationClient aMapLocationClient = new AMapLocationClient(context);
            AMapLocationClientOption aMapLocationClientOption = new AMapLocationClientOption();
            aMapLocationClientOption.setOnceLocation(true);
            aMapLocationClientOption.setNeedAddress(true);
            aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            aMapLocationClient.setLocationOption(aMapLocationClientOption);
            aMapLocationClient.startLocation();
            return aMapLocationClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public Single<IpLocationResponse> getLocation() {
        return LocationRetrofitClient.getLocationApi().getLocation();
    }

    public Single<GeocodeResponse> getGeoCode(String name) {
        return LocationRetrofitClient.getLocationApi().getCityCode(name);
    }
    public Single<ResultPageResultMessageResponseDto> getWarnningApi() {
       return NetworkManager.INSTANCE.getApi().getAlertMessages(0, 1);
    }

    public Single<WeatherResponse> getWeather(String jwd) {
        return LocationRetrofitClient.getWeatherApiService().getWeather(authorization,jwd);
    }

    public Single<AlertResponse> getWarnning(String jd, String wd) {
        return LocationRetrofitClient.getWeatherApiService().getWarnning(authorization,jd, wd);
    }

    public  Single<ResultMessageGroupResponseDto> getMessageUser(int i) {
        return NetworkManager.INSTANCE.getApi().getMyMessages(i, 10);
    }
    public  Single<ResultVoid> isRead(Long i) {
        return NetworkManager.INSTANCE.getApi().markAsRead(i);
    }
    public  Single<ResultVoid> isReadAll() {
        return NetworkManager.INSTANCE.getApi().markAllAsRead();
    }
    public  Single<ResultPostResponseDto> getPost(Long id) {
        return NetworkManager.INSTANCE.getApi().getPostDetail(id);

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
