package com.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationRetrofitClient {
    private static final String URL = "https://restapi.amap.com/";

    private static final String WEATHERURL =
            "https://p34wctngv8.re.qweatherapi.com/";

    private static Retrofit retrofit;

    private static Retrofit weatherRetrofit;
    private static MainApiSerVice apiService;

    private static WeatherApiService weatherApiService;

    public static WeatherApiService getWeatherApiService() {
        if (weatherRetrofit == null) {
            weatherRetrofit = new Retrofit.Builder()
                    .baseUrl(WEATHERURL)
                    //使用rxjava3
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    //使用gson解析
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (weatherApiService == null) {
            weatherApiService = weatherRetrofit.create(WeatherApiService.class);
        }
        return weatherApiService;
    }

    public static MainApiSerVice getLocationApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(URL)
                    //使用rxjava3
                    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                    //使用gson解析
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        if (apiService == null) {
            apiService = retrofit.create(MainApiSerVice.class);
        }
        return apiService;
    }

}
