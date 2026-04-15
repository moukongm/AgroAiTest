package com.main.data;

import android.content.Context;

import com.agri.pest.client.model.request.AdminMessageCreateRequest;
import com.agri.pest.client.model.response.ResultMessageGroupResponseDto;
import com.agri.pest.client.model.response.ResultPageResultMessageResponseDto;
import com.agri.pest.client.model.response.ResultPostResponseDto;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.network.LocationRetrofitClient;
import com.network.NetworkManager;
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
        AdminMessageCreateRequest request1 = new AdminMessageCreateRequest("SYSTEM","【农业气象数据接口升级通知】","尊敬的用户，为了提供更精准的农情预警服务，平台将于2026年4月12日（本周日）02:00-04:00进行气象数据接口升级。期间部分预警推送可能延迟，升级完成后将自动恢复。给您带来的不便敬请谅解。",null,null );
        AdminMessageCreateRequest request = new AdminMessageCreateRequest("ALERT","【蓝色晚霜冻害预警】",
                "受强冷空气影响，预计4月13日～15日早晨最低气温将降至-2℃～0℃，地面温度可达-3℃以下。目前正值苹果花期至幼果期、冬小麦拔节孕穗期，低温霜冻可能导致花器受冻、幼果脱落、小麦结实率下降。",null,null );
        NetworkManager.INSTANCE.getApi().publishMessage(request);
        return NetworkManager.INSTANCE.getApi().publishMessage(request1);
    }

}
