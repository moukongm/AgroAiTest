package com.user.profile.data;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.ChangePhoneRequest;
import com.agri.pest.client.model.request.ProfileUpdateRequest;
import com.agri.pest.client.model.response.AuthResponse;
import com.agri.pest.client.model.response.PageResultPostResponseDto;
import com.agri.pest.client.model.response.PostResponseDto;
import com.agri.pest.client.model.response.ResultPageResultPostResponseDto;
import com.agri.pest.client.model.response.ResultString;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.common.utils.LogUtils;
import com.network.NetworkManager;
import com.user.login.data.LoginRepository;
import com.user.login.data.UserStorageConstant;

import java.util.List;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;

public class UserRemoteDataSource {

    private final LoginRepository loginRepository = new LoginRepository();

    public Single<ResultUserProfileDto> updateUser(ProfileUpdateRequest profileUpdateRequest) {
        return NetworkManager.INSTANCE.getApi().updateProfile(profileUpdateRequest)
                //拦截器，失败了拦截，看看要不要重新发送请求；
                .retryWhen(errors ->
                    //操作符，和subcribe的区别是，前者会返回一个被观察者，后者不会，后者直接做失败或者成功的处理，前者更像是中转站；
                    errors.flatMap(error -> {
                        if(isTokenExpired(error)){
                            //刷新token处理；
                            return refreshTokenAndRetry();
                        }
                        //不是token过期，给上层传回，让他往下传；
                        return Flowable.error(error);
                    }));
    }
    public Single<ResultPageResultPostResponseDto> getFavoritesPosts(int page){
        return NetworkManager.INSTANCE.getApi().getFavoritedPosts(page,10)
                //拦截器，失败了拦截，看看要不要重新发送请求；
                .retryWhen(errors ->
                        //操作符，和subcribe的区别是，前者会返回一个被观察者，后者不会，后者直接做失败或者成功的处理，前者更像是中转站；
                        errors.flatMap(error -> {
                            if(isTokenExpired(error)){
                                //刷新token处理；
                                return refreshTokenAndRetry();
                            }
                            //不是token过期，给上层传回，让他往下传；
                            return Flowable.error(error);
                        }));

    }

    public Single<ResultString> uploadAvatar(MultipartBody.Part part, String fileName) {
        return NetworkManager.INSTANCE.getApi().uploadFile(part, fileName)
                .retryWhen(errors -> errors
                        .flatMap(error -> {
                            if (isTokenExpired(error)) {
                                return refreshTokenAndRetry();
                            }
                            return Flowable.error(error);
                        }));
    }

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

    public Single<ResultVoid> updatePassword(String mima) {
        return NetworkManager.INSTANCE.getApi().updatePassword(mima)
                .retryWhen(errors -> errors
                        .flatMap(error -> {
                            if (isTokenExpired(error)) {
                                return refreshTokenAndRetry();
                            }
                            return Flowable.error(error);
                        }));
    }

    public Single<ResultVoid> updatePhone(ChangePhoneRequest request) {
        LogUtils.INSTANCE.d("ljxphone", "phone");
        return NetworkManager.INSTANCE.getApi().changePhone(request)
                .retryWhen(errors -> errors
                        .flatMap(error -> {
                            if (isTokenExpired(error)) {
                                return refreshTokenAndRetry();
                            }
                            return Flowable.error(error);
                        }));
    }

    public Single<ResultPageResultPostResponseDto> getMinePosts(int page) {
        return NetworkManager.INSTANCE.getApi().getMyPosts(page, 10)
                .retryWhen(errors -> errors
                        .flatMap(error -> {
                            if (isTokenExpired(error)) {
                                return refreshTokenAndRetry();
                            }
                            return Flowable.error(error);
                        }));
    }


    private boolean isTokenExpired(Throwable error) {
        //返回401代表token请求失败
        if (error instanceof retrofit2.HttpException) {
            retrofit2.HttpException httpError = (retrofit2.HttpException) error;
            if (httpError.code() == 401) {
                return true;
            }
        }
        return false;
    }
    private Flowable<?> refreshTokenAndRetry() {
        return loginRepository.refresh()
                .subscribeOn(Schedulers.io())
                .toFlowable()
                .flatMap(response -> {
                    if (response.getCode() == ServiceCode.SUCCESS) {
                        AuthResponse data = response.getData();
                        NetworkManager.INSTANCE.setToken(data.getToken());
                        UserStorageConstant.saveToken(data.getToken());
                        UserStorageConstant.saveRefreshToken(data.getRefreshToken());
                        UserStorageConstant.saveExpiresIn(data.getExpiresIn());
                        LogUtils.INSTANCE.d("TokenRefresh", "token刷新成功");
                        //这里返回空的之后，上层会再次发送请求；
                        return Flowable.empty();
                    } else {
                        //上层会往下传递
                        return Flowable.error(new Exception("刷新token失败"));
                    }
                })
                .onErrorResumeNext(error -> {
                    LogUtils.INSTANCE.e("TokenRefresh", error);
                    return Flowable.error(error);
                });
    }
}
