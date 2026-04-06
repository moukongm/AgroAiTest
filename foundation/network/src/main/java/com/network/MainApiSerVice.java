package com.network;

import com.network.model.GeocodeResponse;
import com.network.model.IpLocationResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface MainApiSerVice {
    @GET("v3/ip?key=d710e3b8d809a7fc4b7f1df33145ff05")
    Single<IpLocationResponse> getLocation();

    @GET("v3/geocode/geo?key=d710e3b8d809a7fc4b7f1df33145ff05")
    Single<GeocodeResponse> getCityCode(
            @Query("address") String address
    );

}
