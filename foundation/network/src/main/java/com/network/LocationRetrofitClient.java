package com.network;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class LocationRetrofitClient {
    private static final String URL = "https://restapi.amap.com/";
    private static final String WEATHERURL = "https://p34wctngv8.re.qweatherapi.com/";

    private static volatile Retrofit retrofit;
    private static volatile Retrofit weatherRetrofit;
    private static volatile MainApiSerVice apiService;
    private static volatile WeatherApiService weatherApiService;

    public static WeatherApiService getWeatherApiService() {
        if (weatherApiService == null) {
            synchronized (LocationRetrofitClient.class) {
                if (weatherApiService == null) {
                    weatherRetrofit = new Retrofit.Builder()
                            .baseUrl(WEATHERURL)
                            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    weatherApiService = weatherRetrofit.create(WeatherApiService.class);
                }
            }
        }
        return weatherApiService;
    }

    public static MainApiSerVice getLocationApi() {
        if (apiService == null) {
            synchronized (LocationRetrofitClient.class) {
                if (apiService == null) {
                    retrofit = new Retrofit.Builder()
                            .baseUrl(URL)
                            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    apiService = retrofit.create(MainApiSerVice.class);
                }
            }
        }
        return apiService;
    }
}
