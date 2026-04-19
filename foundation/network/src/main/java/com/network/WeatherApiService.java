package com.network;

import com.network.model.AlertResponse;
import com.network.model.GeocodeResponse;
import com.network.model.SearchCityResponse;
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
    //获取城市搜索api

    @GET("geo/v2/city/lookup")
    Single<SearchCityResponse> searchCity(
            @Header("X-QW-Api-Key") String authorization,  // 传入 "Bearer your_token"
            @Query("location") String location
    );


}
