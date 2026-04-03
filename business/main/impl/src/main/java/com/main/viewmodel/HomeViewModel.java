package com.main.viewmodel;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import java.util.List;
import java.util.Locale;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.common.base.BaseViewModel;
import com.common.utils.LogUtils;
import com.main.data.Repository;
import com.network.model.AlertResponse;
import com.network.model.WeatherResponse;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeViewModel extends BaseViewModel {

    private final Repository repository = new Repository();
    private final MutableLiveData<String> locationLivedata = new MutableLiveData<>();
    private final MutableLiveData<String> cityCodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> avatarUrlLiveData = new MutableLiveData<>();
    private final MutableLiveData<WeatherResponse.Now> weatherLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<AlertResponse.Alert>> alertLiveData = new MutableLiveData<>();

    public void getLocation(Context context){
        LogUtils.INSTANCE.d("ljx", "定位1");
        repository.getLocation(context).setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if(aMapLocation!=null && aMapLocation.getErrorCode()==0){
                    double x = aMapLocation.getLatitude();
                    double y = aMapLocation.getLongitude();
                    String city = aMapLocation.getCity();
                    Log.d("asdf",city);
                    getWeather(String.valueOf(y) + "," + String.valueOf(x));
                    String formattedLng = String.format(Locale.US, "%.2f", y);
                    String formattedLat = String.format(Locale.US, "%.2f",x);
                    LogUtils.INSTANCE.d("lyy","预警参数 - 经度:" + formattedLng + " 纬度:" + formattedLat);
                    getWarning(formattedLat, formattedLng);
                    }
                else{
                    LogUtils.INSTANCE.d("ljx", "no");
                }
            }
        });
    }



    public void getLocation() {
        LogUtils.INSTANCE.d("ljxtyswy", "load");
        Disposable disposable = repository.getLocation().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getStatus().equals("1")) {
                                LogUtils.INSTANCE.d("lyy","locationnotok");
                                 locationLivedata.setValue(response.getCity());

                            } else {
                                LogUtils.INSTANCE.d("lyy","locationnotok");
                            }

                        },
                        error -> {
//                            LogUtils.INSTANCE.e("lyy", "getLocation error: " + error.getClass().getName() + " - " + error.getMessage());
                            error.printStackTrace();
                        }

                );
        addDisposable(disposable);
    }

    public MutableLiveData<String> getLocationLivedata() {
        return locationLivedata;
    }

//    public void getGeoCode(String name) {
//        Disposable disposable = repository.getGeoCode(name)
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(
//                        response -> {
//                            if ("1".equals(response.getStatus())
//                                    && response.getGeocodes() != null
//                                    && !response.getGeocodes().isEmpty()) {
//                                String location = response.getGeocodes().get(0).getLocation();
//                                String[] parts = location.split(",");
//                                if (parts.length == 2) {
//                                    getWeather(location);
//                                    double lng = Double.parseDouble(parts[0]);
//                                    double lat = Double.parseDouble(parts[1]);
//                                    String formattedLng = String.format(Locale.US, "%.2f", lng);
//                                    String formattedLat = String.format(Locale.US, "%.2f", lat);
//                                    LogUtils.INSTANCE.d("lyy",formattedLat + formattedLng);
//                                    getWarning(formattedLat, formattedLng);
//                                }
//                            }
//                            else{
//                                LogUtils.INSTANCE.d("lyy","codenotok");
//                            }
//                        },
//                        error -> {
//                            LogUtils.INSTANCE.e("lyy",  error);
//                        }
//                );
//        addDisposable(disposable);
//    }

    public MutableLiveData<String> getCityCodeLiveData() {
        return cityCodeLiveData;
    }

    public void getUserInfo() {
        Disposable disposable = repository.getUserMes()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getData() != null) {
                                userNameLiveData.setValue(response.getData().getFullName());
                                avatarUrlLiveData.setValue(response.getData().getAvatarUrl());
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("lyy",  error);
                        }
                );
        addDisposable(disposable);
    }

    public MutableLiveData<String> getUserNameLiveData() {
        return userNameLiveData;
    }

    public MutableLiveData<String> getAvatarUrlLiveData() {
        return avatarUrlLiveData;
    }

    public void getWeather(String jwd) {
        Disposable disposable = repository.getWeather(jwd)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getNow() != null) {
                                weatherLiveData.setValue(response.getNow());
                                LogUtils.INSTANCE.d("lyy",response.getNow().toString());
                            }
                            else{
                                LogUtils.INSTANCE.d("lyy","weathernotok");
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("weather",  error);
                        }
                );
        addDisposable(disposable);
    }

    public MutableLiveData<WeatherResponse.Now> getWeatherLiveData() {
        return weatherLiveData;
    }

    public void getWarning(String jd, String wd) {
        LogUtils.INSTANCE.d("ljx",jd+wd);
        Disposable disposable = repository.getWarning(jd, wd)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getAlerts() != null && !response.getAlerts().isEmpty()) {
//                                LogUtils.INSTANCE.d("lyy",response.getAlerts().get(0).getDescription());
                                alertLiveData.setValue(response.getAlerts());
                            }
                            else{
                                LogUtils.INSTANCE.d("lyy","wrningnotok");
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("warn", error);
                            if (error instanceof retrofit2.HttpException) {
                                retrofit2.HttpException httpError = (retrofit2.HttpException) error;
                                try {
                                    String errorBody = httpError.response().errorBody().string();
                                    LogUtils.INSTANCE.d("warn", "HTTP " + httpError.code() + " body: " + errorBody);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
        addDisposable(disposable);
    }

    public MutableLiveData<List<AlertResponse.Alert>> getAlertLiveData() {
        return alertLiveData;
    }
}
