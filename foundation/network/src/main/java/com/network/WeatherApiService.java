package com.network;

import com.network.model.AlertResponse;
import com.network.model.GeocodeResponse;
import com.network.model.WeatherResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface WeatherApiService {
    //获取天气
    @GET("v7/weather/now?")
    Single<WeatherResponse> getWeather(
            @Header("X-QW-Api-Key") String authorization,
            @Query("location") String mes);

    //获取预警
    // URL: /weatheralert/v1/current/{longitude}/{latitude}
    @GET("weatheralert/v1/current/{longitude}/{latitude}")
    Single<AlertResponse> getWarnning(
            @Header("X-QW-Api-Key") String authorization,
            @Path("longitude") String lon,
            @Path("latitude") String lat);
}
