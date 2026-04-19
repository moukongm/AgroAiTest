package com.user.profile.viewmodel;

import android.content.Context;
import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.model.response.PostResponseDto;
import com.common.base.BaseViewModel;
import com.common.storage.MMKVUtils;
import com.common.storage.database.UserRecord;
import com.common.utils.FileUtils;
import com.common.utils.SingleLiveEvent;
import android.util.Log;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.ChangePhoneRequest;
import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.common.utils.LogUtils;
import com.common.utils.ThreadUtils;
import com.network.NetworkManager;
import com.user.R;
import com.user.profile.data.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ProfileViewModel extends BaseViewModel {

    private  Repository repository;
    private volatile Context appContext;
    private final SingleLiveEvent<String> mesEtLivedata = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> mesEtAvatarLivedata = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> mesNameLivedata = new SingleLiveEvent<>();
    private final MutableLiveData<String> avatarLivedata = new MutableLiveData<>();
    private final MutableLiveData<String> nickNameLivedata = new MutableLiveData<>();
    private final MutableLiveData<Long> historyCountLivedata = new MutableLiveData<>();
    private final SingleLiveEvent<String> phoneLivedata = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> starError = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> passwordLivedata = new SingleLiveEvent<>();

    private final MutableLiveData<String> phoneValueLivedata = new MutableLiveData<>();
    private final MutableLiveData<String> cropsValueLivedata = new MutableLiveData<>();
    private final MutableLiveData<List<PostResponseDto>> minePostsLivedata = new MutableLiveData<>();
    private final MutableLiveData<List<PostResponseDto>> mineFavoritePostsLivedata = new MutableLiveData<>();
    private final MutableLiveData<Long> favoritesCountLivedata = new MutableLiveData<>();
    private final SingleLiveEvent<String> cropsLivedata = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> unLogin = new SingleLiveEvent<>();
    // 标记资料是否已更新（用于通知调用方刷新）
    private final MutableLiveData<Boolean> profileUpdatedLivedata = new MutableLiveData<>(false);


    private int currentPagePost = 0;
    private boolean isLoaddingPost = false;

    private boolean isHasNext = true;

    private int currentPageFavoritePost = 0;
    private boolean isLoaddingFavoritePost = false;

    private boolean isHasFavoriteNext = true;

    public int getCurrentPageFavoritePost() {
        return currentPageFavoritePost;
    }

    private List<PostResponseDto> list = new ArrayList<>();
    private List<PostResponseDto> favoriteList = new ArrayList<>();

    public SingleLiveEvent<String> getStarError() {
        return starError;
    }
    //    public void setpost() {
//        Log.d("xzr", "fabu");
//        List<String> list1 = new ArrayList<>();
//        list1.add("https://s1.aigei.com/src/img/png/86/8624ec6bc43d47ae9a07990cca965d90.png?imageMogr2/auto-orient/thumbnail/!282x282r/gravity/Center/crop/282x282/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:uRsVTTvJlcApVbNOMB7m5S4eD_4=");
//        list1.add("https://s1.aigei.com/src/img/png/3a/3a3643b6c7244fdcac81503ad91d5314.png?imageMogr2/auto-orient/thumbnail/!282x320r/crop/!282x320a0a0/quality/85/%7CimageView2/2/w/282&e=2051020800&token=P7S2Xpzfz11vAkASLTkfHN7Fw-oOZBecqeJaxypL:edv9hg6dnIA8yyppPwrI9dEsWkM=");
//        list1.add("https://tse1.mm.bing.net/th/id/OIP.W97UAGY1NoNMVd8icKgLsAAAAA?rs=1&pid=ImgDetMain&o=7&rm=3");
//        List<String> list2 = new ArrayList<>();
//        list2.add("玉米");
//        list2.add("莲藕");
//        Disposable subscribe = NetworkManager.INSTANCE.getApi().createPost(new PostCreateRequest("不高兴和没头脑", "就是虚招如和刘耀恒", list1
//                        , list2)).observeOn(AndroidSchedulers.mainThread())
//                .subscribeOn(Schedulers.io())
//                .subscribe(
//                        response -> {
//                            if (response.getCode() == ServiceCode.SUCCESS) {
//                                Log.d("xzr", "fabuchenggong");
//                                getFirstPosts();
//                            } else {
//                                Log.d("xzr", "fabushibai");
//                            }
//
//                        },
//                        error -> {
//                            Log.d("xzr", "error");
//                            LogUtils.INSTANCE.d("xzr", error.getMessage());
//                        }
//
//                );
//        addDisposable(subscribe);
//    }

    public void starPost(){
        Disposable subscribe = NetworkManager.INSTANCE.getApi().favoritePost(35).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                Log.d("xzr", "fabuchenggong");
                                getFirstPosts();
                            } else {
                                Log.d("xzr", "fabushibai");
                            }

                        },
                        error -> {
                            Log.d("xzr", "error");
                            LogUtils.INSTANCE.d("xzr", error.getMessage());
                        }

                );
        addDisposable(subscribe);
        Disposable subscribe1 = NetworkManager.INSTANCE.getApi().favoritePost(36).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                Log.d("xzr", "fabuchenggong");
                                getFirstPosts();
                            } else {
                                Log.d("xzr", "fabushibai");
                            }

                        },
                        error -> {
                            Log.d("xzr", "error");
                            LogUtils.INSTANCE.d("xzr", error.getMessage());
                        }

                );
        addDisposable(subscribe1);
    }

    //第一次我的帖子
    public void getFirstPosts() {
        currentPagePost = 0;
        isHasNext = true;
        list.clear();
        loadMinePosts();
    }

    //后续加载
    public void getMorePosts() {
        if (isLoaddingPost) {
            return;
        }
        if (!isHasNext) {
            return;
        }
        loadMinePosts();
    }

    public void initContext(Context context) {
        this.appContext = context.getApplicationContext();
        // 重新创建 repository，确保 UserLocalDataSource 能获取到 Context
        this.repository = new Repository(appContext);
    }

    public void loadMinePosts() {
        LogUtils.INSTANCE.d("ljxtyswy", "load");
        Disposable disposable = repository.getMinePosts(currentPagePost).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                list.addAll(response.getData().getList());
                                isLoaddingPost = false;
                                currentPagePost++;
                                isHasNext = response.getData().getHasNext();
                                minePostsLivedata.setValue(list);
                                LogUtils.INSTANCE.d("ljxty", "ok");
                            } else {
                                LogUtils.INSTANCE.d("ljxty", "notok");
                            }

                        },
                        error -> {
                            LogUtils.INSTANCE.e("ljxty", error);
                        }

                );
        addDisposable(disposable);
    }

    public void unLogin() {
        repository.unLogin();
        ThreadUtils.INSTANCE.runOnUiThreadDelayed(new Runnable() {
            @Override
            public void run() {
                unLogin.setValue("yes");
//                fragment.requireActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        }, 1000);

    }

    //获取用户消息
    public void getUserMes() {
        if (!isNetworkConnected()) {
            LogUtils.INSTANCE.d("ProfileViewModel", "无网络连接，从本地加载数据");
            loadUserDataFromLocal();
            return;
        }

        Disposable disposable = repository.getUserMes().observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                LogUtils.INSTANCE.d("asdfghjkl", "nameok");
                                nickNameLivedata.setValue(response.getData().getFullName());
                                avatarLivedata.setValue(response.getData().getAvatarUrl());
                                phoneValueLivedata.setValue(response.getData().getPhone());
                                String cropsReslut = response.getData().getFollowedCrops().toString();
                                cropsValueLivedata.setValue(cropsReslut.substring(1, cropsReslut.length() - 1));
                                phoneLivedata.setValue("获取成功");
                                favoritesCountLivedata.setValue(response.getData().getFavoriteCount());
                                historyCountLivedata.setValue(response.getData().getHistoryRecognitionCount());
                                // 通知数据加载完成
                                userProfileMes.setValue(response);
                            } else {
                                // 即使返回错误也尝试从本地加载
                                loadUserDataFromLocal();
                            }

                        },
                        error -> {
                            LogUtils.INSTANCE.d(error.getMessage());
                            loadUserDataFromLocal();
                        }

                );
        addDisposable(disposable);
    }


    private boolean isNetworkConnected() {
        if (appContext == null) {
            return false;
        }
        android.net.ConnectivityManager cm = (android.net.ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            android.net.NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null &&
                   (capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET));
        } else {
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
    }


    private void loadUserDataFromLocal() {
        if (appContext == null) {
            LogUtils.INSTANCE.d("ProfileViewModel", "appContext is null, cannot load local data");
            userProfileMes.postValue(null); // 通知加载完成
            return;
        }

        ThreadUtils.INSTANCE.executeByIo(() -> {
            try {
                UserRecord user = repository.getLocalUser();
                if (user != null) {
                    ThreadUtils.INSTANCE.runOnUiThread(() -> {
                        nickNameLivedata.setValue(repository.getLocalNickname());
                        if (user.getAvatarLocalPath() != null && !user.getAvatarLocalPath().isEmpty()) {
                            avatarLivedata.setValue("file://" + user.getAvatarLocalPath());
                        } else if (user.getAvatarUrl() != null) {
                            avatarLivedata.setValue(user.getAvatarUrl());
                        }
                    });
                }
                int detectionCount = repository.getLocalDetectionCount();
                ThreadUtils.INSTANCE.runOnUiThread(() -> {
                    historyCountLivedata.setValue((long) detectionCount);
                    userProfileMes.postValue(null);
                });
                LogUtils.INSTANCE.d("ProfileViewModel", "本地数据加载完成");
            } catch (Exception e) {
                LogUtils.INSTANCE.e("ProfileViewModel", "load local data failed", e);
                userProfileMes.postValue(null);
            }
        });
    }

    //第一次我的收藏帖子
    public void getFirstFavoritePosts() {
        currentPageFavoritePost = 0;
        isHasFavoriteNext = true;
        favoriteList.clear();
        loadMineFavoritePosts();
    }

    //后续收藏帖子加载
    public void getMoreFavoritePosts() {
        if (isLoaddingFavoritePost) {
            return;
        }
        if (!isHasFavoriteNext) {
            return;
        }
        loadMineFavoritePosts();
    }

    //加载收藏帖子
    public void loadMineFavoritePosts() {
        Disposable disposable = repository.getFavoritesPosts(currentPageFavoritePost).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                favoriteList.addAll(response.getData().getList());
                                isLoaddingFavoritePost = false;
                                currentPageFavoritePost++;
                                isHasFavoriteNext = response.getData().getHasNext();
                                mineFavoritePostsLivedata.setValue(favoriteList);
                            } else {
                                starError.setValue("糟糕，服务端没返回");
                            }

                        },
                        error -> {
                            starError.setValue(error.toString());
                            LogUtils.INSTANCE.d(error.getMessage());
                        }

                );
        addDisposable(disposable);
    }

    public void updatePhone(ChangePhoneRequest request) {
        LogUtils.INSTANCE.d("ljxphone", "phonevm");
        Disposable disposable = repository.updatePhone(request).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                phoneValueLivedata.setValue(request.getNewPhone());
                                repository.updateLocalTele(request.getNewPhone());
                                LogUtils.INSTANCE.d("ljxphone", "phoneok");
                                phoneLivedata.setValue("修改成功");
                            } else {
                                LogUtils.INSTANCE.d("ljxphone", response.getCode() +response.getMessage());
                                phoneLivedata.setValue("修改失败"+response.getMessage());
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("ljxphone", error);
                            phoneLivedata.setValue("修改失败"+error.getMessage());
                        }

                );
        addDisposable(disposable);
    }

    //修改资料
    public void updataProfile(ProfileUpdateRequest request, String updateContent) {
        Disposable disposable = repository.updateUser(request).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response != null && response.getCode() == ServiceCode.SUCCESS) {
                                updataProfileUtilMethod("ok", updateContent, response);
                            } else {
                                String errorMsg = (response != null) ? response.getMessage() : "修改失败";
                                updataProfileUtilMethod("no", updateContent, null, errorMsg);
                            }
                        },
                        error -> {
                            LogUtils.INSTANCE.e("ProfileViewModel", "updataProfile error", error);
                            String errorMsg = "网络错误";
                            if (error instanceof retrofit2.HttpException) {
                                int code = ((retrofit2.HttpException) error).code();
                                if (code == 403) {
                                    errorMsg = "权限不足，请重新登录";
                                } else if (code == 401) {
                                    errorMsg = "登录已过期，请重新登录";
                                }
                            }
                            updataProfileUtilMethod("no", updateContent, null, errorMsg);
                        }

                );
        addDisposable(disposable);
    }

    public void updataProfileUtilMethod(String res, String
            updateContent, ResultUserProfileDto response) {
        updataProfileUtilMethod(res, updateContent, response, null);
    }

    public void updataProfileUtilMethod(String res, String
            updateContent, ResultUserProfileDto response, String errorMsg) {
        switch (updateContent) {
            case "fullName":
                // 处理 fullName 更新
                if ("ok".equals(res)) {
                    if (response != null && response.getData() != null) {
                        mesEtLivedata.setValue("修改成功");
                        repository.updateLocalNickname(response.getData().getFullName());
                        nickNameLivedata.setValue(response.getData().getFullName());
                        profileUpdatedLivedata.setValue(true); // 标记资料已更新
                        MMKVUtils.INSTANCE.custom("user_module").put("username",response.getData().getFullName());
                        LogUtils.INSTANCE.d("ljx", "nameok");
                    } else {
                        mesEtLivedata.setValue("修改失败：数据异常");
                    }
                } else {
                    mesEtLivedata.setValue(errorMsg != null ? errorMsg : "修改失败");
                }
                break;
            case "avatarUrl":
                // 处理 avatarUrl 更新
                if ("ok".equals(res)) {
                    if (response != null && response.getData() != null) {
                        String newAvatarUrl = response.getData().getAvatarUrl();
                        avatarLivedata.setValue(newAvatarUrl);
                        mesEtAvatarLivedata.setValue("修改成功");
                        profileUpdatedLivedata.setValue(true);


                        if (appContext != null) {
                            long userId = 0;

                            try {
                                com.common.utils.AvatarUtils.deleteAvatar(appContext, userId);
                                LogUtils.INSTANCE.d("ProfileViewModel", "old avatar deleted for userId: " + userId);
                            } catch (Exception e) {
                                LogUtils.INSTANCE.e("ProfileViewModel", "delete old avatar failed", e);
                            }


                            ThreadUtils.INSTANCE.executeByIo(() -> {
                                String localPath = com.common.utils.AvatarUtils.downloadAndSaveAvatar(appContext, newAvatarUrl, userId);
                                if (localPath != null) {
                                    repository.updateLocalAvatar(localPath);
                                    LogUtils.INSTANCE.d("ProfileViewModel", "new avatar downloaded and saved: " + localPath);
                                } else {
                                    LogUtils.INSTANCE.d("ProfileViewModel", "download new avatar failed");
                                }
                            });
                        }
                    } else {
                        avatarLivedata.setValue("修改失败：数据异常");
                    }
                } else {
                    avatarLivedata.setValue(errorMsg != null ? errorMsg : "修改失败");
                }
                break;
            case "bio":
                // 处理 bio 更新
                if ("ok".equals(res)) {

                } else {

                }
                break;
            case "location":
                // 处理 location 更新
                if ("ok".equals(res)) {

                } else {

                }
                break;
            case "followedCrops":
                // 处理 followedCrops 更新
                if ("ok".equals(res)) {
                    if (response != null && response.getData() != null) {
                        cropsLivedata.setValue("修改成功");
                        String cropsReslut = response.getData().getFollowedCrops().toString();
                        cropsValueLivedata.setValue(cropsReslut.substring(1, cropsReslut.length() - 1));
                        LogUtils.INSTANCE.d("ljx", "cropsok");
                    } else {
                        cropsLivedata.setValue("修改失败：数据异常");
                    }
                } else {
                    cropsLivedata.setValue(errorMsg != null ? errorMsg : "修改失败");
                }
                break;
            default:
                // 未知字段处理
                break;
        }
    }

    //修改密码
    public void updatePassword(String password) {
        Disposable disposable = repository.updatePassword(password).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS) {
                                repository.updateLocalPassword(password);
                                passwordLivedata.setValue("修改成功，请重新登录");
                                unLogin();
                            } else {
                                passwordLivedata.setValue("修改失败");
                            }
                        },
                        error -> {
                            passwordLivedata.setValue("修改失败");
                        }

                );
        addDisposable(disposable);
    }

    //请求体的构建
//    private MultipartBody.Part prepareFilePart(Context context, Uri uri) {
//        File file = getFilePath(context, uri);
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
//        //第一给服务端识别参数，第二文件名，第三文件；
//        return MultipartBody.Part.createFormData("avatar", file.getName(), requestBody);
//    }


        // 上传头像（ViewModel 不依赖 Context）
        public void uploadAvatar (File file){
            if (file == null || !file.exists()) {
                mesEtAvatarLivedata.postValue("文件解析失败");
                return;
            }
            String mimeType = FileUtils.INSTANCE.getMimeType(file);
            RequestBody requestBody = RequestBody.create(MediaType.parse(mimeType), file);
            MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
            addDisposable(
                    repository.uploadAvatar(part, "uploads/demo/")
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .subscribe(
                                    response -> {
                                        if (response.getCode() == ServiceCode.SUCCESS) {
                                            ProfileUpdateRequest request = new ProfileUpdateRequest(null,
                                                    response.getData(), null, null, null);
                                            updataProfile(request, "avatarUrl");
                                            LogUtils.INSTANCE.d("ljx", "ok");
                                        } else {
                                            mesEtAvatarLivedata.setValue("上传失败");
                                        }
                                    },
                                    error -> {
                                        LogUtils.INSTANCE.e("ljx", error);
                                        mesEtAvatarLivedata.setValue("上传失败");
                                    }
                            ));
        }

        //把uri转为文件给服务端
//    private File getFilePath(Context context, Uri uri) {
//        //第一个参数是放在app的缓存文件夹，系统会自动清理；第二个参数是文件名，可以避免重复而且表明是图片
//        File file = new File(context.getCacheDir(), System.currentTimeMillis() + ".jpg");
//
//        try {
//            //通过内容提供器，把uri的数据读出来
//            InputStream inputStream = context.getContentResolver().openInputStream(uri);
//            //写到文件里
//            FileOutputStream outputStream = new FileOutputStream(file);
//            //照片资源可能过大，避免内存爆炸
//            byte[] buffer = new byte[1024];
//            int len;
//            if (inputStream != null) {
//                while ((len = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, len);
//                }
//                inputStream.close();
//            }
//            outputStream.close();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return file;
//
//    }

        //验证名字合法
        private boolean isNicknameVaild (String newName){
            int length = newName.length();
            if (!newName.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$")) {
                mesNameLivedata.setValue("昵称包含@<>/等字符，请修改后重试");
                return false;
            }
            if (length < 2 || length > 24) {
                mesNameLivedata.setValue("请设置2-24个字符");
                return false;
            }
            mesNameLivedata.setValue("ok");
            return true;
        }

        private final MutableLiveData<ResultUserProfileDto> userProfileMes = new MutableLiveData<>();

        public SingleLiveEvent<String> getMesEtLivedata () {
            return mesEtLivedata;
        }

        public MutableLiveData<String> getCropsLivedata () {
            return cropsLivedata;
        }

        public MutableLiveData<String> getCropsValueLivedata () {
            return cropsValueLivedata;
        }

        public MutableLiveData<String> getPhoneValueLivedata () {
            return phoneValueLivedata;
        }


        public MutableLiveData<List<PostResponseDto>> getMinePostsLivedata () {
            return minePostsLivedata;
        }

        public MutableLiveData<List<PostResponseDto>> getMineFavoritePostsLivedata () {
            return mineFavoritePostsLivedata;
        }

        public ProfileViewModel() {
            repository = new Repository();  // 先初始化为无参构造，等 initContext 时再重新初始化
        }

        public SingleLiveEvent<String> getPhoneLivedata () {
            return phoneLivedata;
        }

        public SingleLiveEvent<String> getMesEtAvatarLivedata () {
            return mesEtAvatarLivedata;
        }

        public SingleLiveEvent<String> getMesEtnameLivedata () {
            return mesEtLivedata;
        }

        public SingleLiveEvent<String> getMesNameLivedata () {
            return mesNameLivedata;
        }

        public MutableLiveData<String> getAvatarLivedata () {
            return avatarLivedata;
        }

        public MutableLiveData<String> getNickNameLivedata () {
            return nickNameLivedata;
        }

        public MutableLiveData<Long> getFavoritesCountLivedata () {
            return favoritesCountLivedata;
        }

        public SingleLiveEvent<String> getPasswordLivedata () {
            return passwordLivedata;
        }

        public MutableLiveData<ResultUserProfileDto> getUserProfileMes () {
            return userProfileMes;
        }

        public SingleLiveEvent<String> getUnLogin () {
            return unLogin;
        }

        public MutableLiveData<Long> getHistoryCountLivedata () {
            return historyCountLivedata;
        }

        public MutableLiveData<Boolean> getProfileUpdatedLivedata () {
            return profileUpdatedLivedata;
        }
}

