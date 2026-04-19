package com.user.login.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.PostCreateRequest;
import com.agri.pest.client.model.response.ResultAuthResponse;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.common.base.BaseViewModel;
import com.user.login.data.LoginRepository;
import com.user.profile.data.Repository;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginViewModel extends BaseViewModel {
    public MutableLiveData<ResultAuthResponse> loginResultLiveData = new MutableLiveData<ResultAuthResponse>();

    public MutableLiveData<ResultAuthResponse> registerResultLiveData = new MutableLiveData<ResultAuthResponse>();

    public MutableLiveData<String> errorLiveData = new MutableLiveData<String>();

    private MutableLiveData<String> toastMsg = new MutableLiveData<>("");

    private MutableLiveData<Boolean> agreeChecked = new MutableLiveData<>(false);

    private final LoginRepository repository = new LoginRepository();
    private final Repository profileRepository = new Repository();

    private final MutableLiveData<ResultUserProfileDto> userProfileLiveData = new MutableLiveData<>();


    public void login(String usernameOrPhone, String password) {
        String phoneError = repository.validatePhone(usernameOrPhone);
        if (phoneError != null) {
            toastMsg.setValue(phoneError);
            return;
        }
        String passwordError = repository.validatePassword(password);
        if (passwordError != null) {
            toastMsg.setValue(passwordError);
            return;
        }
        if (!(agreeChecked.getValue())) {
            toastMsg.setValue("请同意用户协议和隐私政策");
            return;
        }
        Disposable disposable = repository.login(usernameOrPhone, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getCode() == ServiceCode.SUCCESS) {
                        repository.saveUserInfo(response.getData());
                        repository.savePassword(password);
                        toastMsg.setValue("登录成功");
                    } else {
                        toastMsg.setValue("登录失败" + repository.errorCode(response.getCode()));
                    }
                    loginResultLiveData.setValue(response);
                }, error -> {
                    errorLiveData.setValue("登录失败:" + error.getMessage());
                });
        addDisposable(disposable);
    }

    public void register(String phone, String password, String username) {
        String phoneError = repository.validatePhone(phone);
        if (phoneError != null) {
            toastMsg.setValue(phoneError);
            return;
        }
        Disposable disposable = repository.register(phone, password, username)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getCode() == ServiceCode.SUCCESS) {
                        repository.saveUserInfo(response.getData());
                    } else {
                        toastMsg.setValue("注册失败" + repository.errorCode(response.getCode()));
                    }
                    registerResultLiveData.setValue(response);
                }, error -> {
                    errorLiveData.setValue("注册失败:" + error.getMessage());
                });
        addDisposable(disposable);
    }


    public MutableLiveData<ResultAuthResponse> getLoginResultLiveData() {
        return loginResultLiveData;
    }

    public MutableLiveData<ResultAuthResponse> getRegisterResultLiveData() {
        return registerResultLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<String> getToastMsg() {
        return toastMsg;
    }

    public void setAgreeChecked(Boolean checked) {
        agreeChecked.setValue(checked);
    }

    public MutableLiveData<ResultUserProfileDto> getUserProfileLiveData() {
        return userProfileLiveData;
    }

    public void getUserMes() {
        Disposable disposable = profileRepository.getUserMes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        response -> {
                            if (response.getCode() == ServiceCode.SUCCESS && response.getData() != null) {
                                userProfileLiveData.setValue(response);
                            } else {
                                userProfileLiveData.setValue(null);
                            }
                        },
                        error -> {
                            userProfileLiveData.setValue(null);
                        }
                );
        addDisposable(disposable);
    }
}
