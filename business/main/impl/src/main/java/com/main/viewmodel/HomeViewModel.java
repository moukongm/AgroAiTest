package com.main.viewmodel;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Locale;
import java.util.List;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.agri.pest.client.model.response.MessageResponseDto;
import com.agri.pest.client.model.response.PageResultMessageResponseDto;
import com.agri.pest.client.model.response.ResultPageResultMessageResponseDto;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.common.base.BaseViewModel;
import com.common.notice.BusKey;
import com.common.notice.LiveDataBus;
import com.common.storage.database.AppDatabase;
import com.common.storage.database.CropDao;
import com.common.storage.database.CropRecord;
import com.common.storage.database.DetectionDao;
import com.common.storage.database.DetectionRecord;
import com.common.storage.database.UserDao;
import com.common.storage.database.UserRecord;
import com.common.utils.AvatarUtils;
import com.common.utils.LogUtils;
import com.agri.pest.client.model.response.MyCropResponseDto;
import com.agri.pest.client.model.response.ResultListMyCropResponseDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.common.utils.SingleLiveEvent;
import com.common.utils.ThreadUtils;
import com.main.data.Repository;
import com.network.NetworkManager;
import com.network.model.AlertResponse;
import com.network.model.LocationItem;
import com.network.model.WeatherResponse;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeViewModel extends BaseViewModel {

    private final Repository repository = new Repository();
    private final MutableLiveData<String> locationLivedata = new MutableLiveData<>();
    private final MutableLiveData<String> cityCodeLiveData = new MutableLiveData<>();
    private final MutableLiveData<Long> historyCountLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> userNameLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> avatarUrlLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<LocationItem>> cityLiveData = new MutableLiveData<>();
    private final SingleLiveEvent<String> updateLocationResult = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> userLocationLiveData = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> cityError = new SingleLiveEvent<>();
    private final MutableLiveData<WeatherResponse.Now> weatherLiveData = new MutableLiveData<>();

    private final MutableLiveData<MessageResponseDto> alertLiveData = new MutableLiveData<>();
    AMapLocationClient location;
    AMapLocationListener listener;

    //private final MutableLiveData<List<AlertResponse.Alert>> alertLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<MyCropResponseDto>> cropListLiveData = new MutableLiveData<>();
    private final MutableLiveData<UserRecord> userRecordLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<CropRecord>> localCropListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isOfflineModeLiveData = new MutableLiveData<>(false);

    public MutableLiveData<String> getCityError() {
        return cityError;
    }

    public MutableLiveData<List<LocationItem>> getCityLiveData() {
        return cityLiveData;
    }

    public SingleLiveEvent<String> getUpdateLocationResult() {
        return updateLocationResult;
    }

    public MutableLiveData<Long> getHistoryCountLiveData() {
        return historyCountLiveData;
    }

    public SingleLiveEvent<String> getUserLocationLiveData() {
        return userLocationLiveData;
    }

    private volatile boolean isCropLoading = false;

    public void getLocation(Context context) {
        LogUtils.INSTANCE.d("ljx", "定位1");
        listener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null && aMapLocation.getErrorCode() == 0) {
                    double x = aMapLocation.getLatitude();
                    double y = aMapLocation.getLongitude();
                    String city = aMapLocation.getCity();
                    Log.d("asdf", city);
                    locationLivedata.setValue(city);
                    // 定位成功，保存到数据库
                    saveLocationToDatabase(city);
                    // 发送消息给 CityDefaultFragment 更新 tv_locationCity
                    LiveDataBus.getInstance().with(BusKey.LOCATION_CITY).setValue(city);
                    getWeather(String.valueOf(y) + "," + String.valueOf(x));
//                    String formattedLng = String.format(Locale.US, "%.2f", y);
//                    String formattedLat = String.format(Locale.US, "%.2f",x);
//                    LogUtils.INSTANCE.d("lyy","预警参数 - 经度:" + formattedLng + " 纬度:" + formattedLat);
                    getWarning();
                }else{
                    locationLivedata.setValue("");
                    LogUtils.INSTANCE.d("dfgh",aMapLocation.getErrorCode()+aMapLocation.getErrorInfo()+aMapLocation.getLocationDetail());
                    LiveDataBus.getInstance().with(BusKey.LOCATION_CITY).setValue("");
                    loadLocationFromDb();
                }
            }
        };
        location = repository.getLocation(context);
        location.setLocationListener(listener);
    }

    public void getCity(String jwd){
        Disposable disposable = repository.getCity(jwd)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode().equals("200")) {
                                LogUtils.INSTANCE.d("xzrljxyes",response.getLocation().toString());
                               cityLiveData.setValue(response.getLocation());
                            }else{
                                LogUtils.INSTANCE.d("xzrljxyes",response.getCode()+"");
                                cityError.setValue("搜索失败");
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("xzrljxyes",  error);
                            cityError.setValue("搜索失败"+error.getMessage());
                        }
                );
        addDisposable(disposable);
    }

    public void updateLocation(String cityName){
        ProfileUpdateRequest request = new ProfileUpdateRequest(null,null,null,cityName,null);
        Disposable disposable = repository.updateLocation(request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() == ServiceCode.SUCCESS) {
                               // 返回的是城市名称，用于更新 tv_currentCity
                               updateLocationResult.setValue(cityName);
                               // 更新位置后，获取该位置的天气
                               getGeoCode(cityName);
                            }else{
                                cityError.setValue("更新失败");
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("lyy",  error);
                            cityError.setValue("更新失败"+error.getMessage());
                        }
                );
        addDisposable(disposable);
    }

    public MutableLiveData<String> getLocationLivedata() {
        return locationLivedata;
    }



    public void getGeoCode(String name) {
        Disposable disposable = repository.getGeoCode(name)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if ("1".equals(response.getStatus())
                                    && response.getGeocodes() != null
                                    && !response.getGeocodes().isEmpty()) {
                                String location = response.getGeocodes().get(0).getLocation();
                                String[] parts = location.split(",");
                                if (parts.length == 2) {
                                    getWeather(location);

                                }
                            }
                            else{
                                LogUtils.INSTANCE.d("lyy","codenotok");
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("lyy",  error);
                        }
                );
        addDisposable(disposable);
    }

    public MutableLiveData<String> getCityCodeLiveData() {
        return cityCodeLiveData;
    }

    private volatile Context appContext;

    public HomeViewModel() {

    }

    public void initContext(Context context) {
        this.appContext = context.getApplicationContext();
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
                                historyCountLiveData.setValue(response.getData().getHistoryRecognitionCount());
                                userLocationLiveData.setValue(response.getData().getLocation());
                                saveUserToDatabase(response.getData().getAvatarUrl());
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("lyy",  error);
                            // 网络失败时尝试从数据库加载
                            loadUserFromDatabase();
                        }
                );
        addDisposable(disposable);
    }


    public void loadLocationFromDb() {
        loadLocationFromDatabase();
    }

    private void saveUserToDatabase(String avatarUrl) {
        if (appContext == null) {
            return;
        }
        ThreadUtils.INSTANCE.executeByIo(() -> {
            UserDao userDao = AppDatabase.Companion.getInstance(appContext).userDao();
            AvatarUtils.deleteAvatar(appContext, 0);
            String localPath = AvatarUtils.downloadAndSaveAvatar(appContext, avatarUrl, 0);
            
            // 先查询现有用户，保留其他字段
            UserRecord userRecord = userDao.getUserById(0);
            if (userRecord == null) {
                userRecord = new UserRecord();
                userRecord.setUserId(0L);
            }
            userRecord.setAvatarUrl(avatarUrl);
            userRecord.setAvatarLocalPath(localPath);
            userRecord.setLastUpdateTime(System.currentTimeMillis());
            userDao.insert(userRecord);
            userRecordLiveData.postValue(userRecord);
        });
    }

    private void loadUserFromDatabase() {
        if (appContext == null) {
            return;
        }
        ThreadUtils.INSTANCE.executeByIo(() -> {
            UserDao userDao = AppDatabase.Companion.getInstance(appContext).userDao();
            UserRecord userRecord = userDao.getUserById(0);
            DetectionDao detectionDao = AppDatabase.Companion.getInstance(appContext).detectionDao();
            int detection = detectionDao.getCount();
            if (userRecord != null) {
                userRecordLiveData.postValue(userRecord);
                if (userRecord.getAvatarUrl() != null) {
                    avatarUrlLiveData.postValue(userRecord.getAvatarUrl());
                }
            }
            ThreadUtils.INSTANCE.runOnUiThread(() -> {
                historyCountLiveData.setValue(Long.valueOf(detection));
            });
        });
    }

    public MutableLiveData<UserRecord> getUserRecordLiveData() {
        return userRecordLiveData;
    }

    public MutableLiveData<String> getUserNameLiveData() {
        return userNameLiveData;
    }

    public MutableLiveData<String> getAvatarUrlLiveData() {
        return avatarUrlLiveData;
    }

    public void getWeather(String jwd) {
        LogUtils.INSTANCE.d("ftgbjsdkahkfhad,ukfhkashfa", "true");
        Disposable disposable = repository.getWeather(jwd)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getNow() != null) {
                                weatherLiveData.setValue(response.getNow());
                                LogUtils.INSTANCE.d("ftgbjsdkahkfhad,ukfhkashfa", response.getNow().toString());
                            } else {
                                LogUtils.INSTANCE.d("ftgbjsdkahkfhad,ukfhkashfa", "weathernotok");
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("ftgbjsdkahkfhad,ukfhkashfa", error);
                        }
                );
        addDisposable(disposable);
    }

    public MutableLiveData<WeatherResponse.Now> getWeatherLiveData() {
        return weatherLiveData;
    }

    public void getWarning() {
        Disposable disposable = repository.getWarnningApi()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            LogUtils.INSTANCE.d("warn_sunwenyu", response.toString());
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                PageResultMessageResponseDto data = response.getData();
                                MessageResponseDto messageResponseDto = null;
                                if (data != null && data.getList() != null && !data.getList().isEmpty()) {
                                    messageResponseDto = data.getList().get(0);
                                }
                                alertLiveData.setValue(messageResponseDto);
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("warn", error);
                            if (error instanceof retrofit2.HttpException) {
                                retrofit2.HttpException httpError = (retrofit2.HttpException) error;
                                try {
                                    if (httpError.response() != null && httpError.response().errorBody() != null) {
                                        String errorBody = httpError.response().errorBody().string();
                                        LogUtils.INSTANCE.d("warn", "HTTP " + httpError.code() + " body: " + errorBody);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                );
        addDisposable(disposable);
    }

    //碎片或者活动或者调用ViewModelStore.clear()时触发;
    @Override
    protected void onCleared() {
        super.onCleared();
        stopLocation();
    }

    public void stopLocation() {
        if (location != null) {
            if (listener != null) {
                location.unRegisterLocationListener(listener);
            }
            location.stopLocation();
            location.onDestroy();
            location = null;
            listener = null;
        }
    }

    public MutableLiveData<MessageResponseDto> getAlertLiveData() {
        return alertLiveData;
    }

    public MutableLiveData<List<MyCropResponseDto>> getCropListLiveData() {
        return cropListLiveData;
    }

    public MutableLiveData<List<CropRecord>> getLocalCropListLiveData() {
        return localCropListLiveData;
    }

    public MutableLiveData<Boolean> getIsOfflineModeLiveData() {
        return isOfflineModeLiveData;
    }
    private void saveLocationToDatabase(String city) {
        if (appContext == null) {
            LogUtils.INSTANCE.d("HomeViewModel", "appContext is null, skip saving location");
            return;
        }
        ThreadUtils.INSTANCE.executeByIo(() -> {
            try {
                UserDao userDao = AppDatabase.Companion.getInstance(appContext).userDao();
                UserRecord user = userDao.getUserById(0);
                if (user != null) {
                    user.setLocation(city);
                    userDao.update(user);
                    LogUtils.INSTANCE.d("HomeViewModel", "location saved to database: " + city);
                } else {
                    // 创建新用户记录
                    UserRecord newUser = new UserRecord();
                    newUser.setUserId(0L);
                    newUser.setLocation(city);
                    userDao.insert(newUser);
                    LogUtils.INSTANCE.d("HomeViewModel", "user created with location: " + city);
                }
            } catch (Exception e) {
                LogUtils.INSTANCE.e("HomeViewModel", "save location to database failed", e);
            }
        });
    }
    
    private void loadLocationFromDatabase() {
        if (appContext == null) {
            LogUtils.INSTANCE.d("HomeViewModel", "appContext is null, cannot load location");
            return;
        }
        ThreadUtils.INSTANCE.executeByIo(() -> {
            try {
                UserDao userDao = AppDatabase.Companion.getInstance(appContext).userDao();
                UserRecord user = userDao.getUserById(0);
                if (user != null && user.getLocation() != null) {
                    String location = user.getLocation();
                    ThreadUtils.INSTANCE.runOnUiThread(() -> {
                        locationLivedata.setValue(location);
                        LogUtils.INSTANCE.d("HomeViewModel", "location loaded from database: " + location);
                        // 根据加载的位置获取天气
                        getGeoCode(location);
                    });
                }
            } catch (Exception e) {
                LogUtils.INSTANCE.e("HomeViewModel", "load location from database failed", e);
            }
        });
    }

    public void getMyCrops() {
        Disposable disposable = repository.getMyCrops()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            LogUtils.INSTANCE.d("HomeViewModel", "getMyCrops: 网络返回, code=" + (response != null ? response.getCode() : "null"));
                            if (response != null && response.getCode() == 200 && response.getData() != null) {
                                cropListLiveData.setValue(response.getData());
                                saveCropsToDatabase(response.getData());
                                isOfflineModeLiveData.setValue(false);
                            } else {
                                LogUtils.INSTANCE.d("HomeViewModel", "getMyCrops: 获取作物列表失败");
                                cropListLiveData.setValue(null);
                                loadCropsFromDatabase();
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("HomeViewModel", "getMyCrops: 网络请求失败", error);
                            cropListLiveData.setValue(null);
                            loadCropsFromDatabase();
                        }
                );
        addDisposable(disposable);
    }

    private void saveCropsToDatabase(List<MyCropResponseDto> crops) {
        if (appContext == null) {
            return;
        }
        ThreadUtils.INSTANCE.executeByIo(() -> {
            CropDao cropDao = AppDatabase.Companion.getInstance(appContext).cropDao();
            UserDao userDao = AppDatabase.Companion.getInstance(appContext).userDao();
            // 获取用户头像路径
            UserRecord userRecord = userDao.getUserById(0);
            String avatarLocalPath = userRecord != null ? userRecord.getAvatarLocalPath() : null;
            // 先清除所有数据
            cropDao.deleteAll();
            // 最多存三条（仅作物数据）
            int count = Math.min(crops.size(), 3);
            for (int i = 0; i < count; i++) {
                MyCropResponseDto crop = crops.get(i);
                CropRecord record = new CropRecord();
                record.setCropId(crop.getId());
                record.setCropName(crop.getPlantName());
                record.setCropImageUrl(crop.getImageUrl());
                // 使用AvatarUtils存储作物图片到本地
                String localPath = AvatarUtils.downloadAndSaveAvatar(appContext, crop.getImageUrl(), i + 1);
                record.setCropImageUrl(localPath);
                cropDao.insert(record);
            }
            // 更新所有作物记录的用户头像路径
            if (avatarLocalPath != null) {
                List<CropRecord> allCrops = cropDao.getAllCrops();
                for (CropRecord crop : allCrops) {
                    crop.setAvatarLocalPath(avatarLocalPath);
                    cropDao.update(crop);
                }
            }
            // 通知UI更新（使用数据库新增的方法取最新的最多3条）
            List<CropRecord> savedCrops = cropDao.getRecentCrops();
            localCropListLiveData.postValue(savedCrops);
        });
    }

    private void loadCropsFromDatabase() {
        if (appContext == null) {
            return;
        }
        ThreadUtils.INSTANCE.executeByIo(() -> {
            CropDao cropDao = AppDatabase.Companion.getInstance(appContext).cropDao();
            UserDao userDao = AppDatabase.Companion.getInstance(appContext).userDao();
            // 加载用户信息
            UserRecord userRecord = userDao.getUserById(0);
            if (userRecord != null) {
                userRecordLiveData.postValue(userRecord);
                if (userRecord.getAvatarUrl() != null) {
                    avatarUrlLiveData.postValue(userRecord.getAvatarUrl());
                }
            }
            // 加载作物数据（最多3条最新）
            List<CropRecord> crops = cropDao.getRecentCrops();
            isOfflineModeLiveData.postValue(true);
            localCropListLiveData.postValue(crops);
        });
    }

    public void deleteCrop(Long cropId) {
        Disposable disposable = repository.deleteCrop(cropId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() == 200) {
                                LogUtils.INSTANCE.d("HomeViewModel", "删除作物成功");
                                // 清空旧数据，重新加载
                                cropListLiveData.setValue(null);
                                getMyCrops();
                            } else {
                                LogUtils.INSTANCE.e("删除作物失败: " + response.getMessage(), null);
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("删除作物失败: " + error.getMessage(), null);
                        }
                );
        addDisposable(disposable);
    }
}
