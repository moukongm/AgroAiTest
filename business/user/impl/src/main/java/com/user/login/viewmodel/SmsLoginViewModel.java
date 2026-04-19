package com.user.login.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.response.ResultAuthResponse;
import com.agri.pest.client.model.response.ResultUserProfileDto;
import com.agri.pest.client.model.response.ResultVoid;
import com.common.base.BaseViewModel;
import com.user.login.data.LoginRepository;
import com.user.profile.data.Repository;

import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SmsLoginViewModel extends BaseViewModel {
    public MutableLiveData<ResultAuthResponse> loginResultLiveData = new MutableLiveData<ResultAuthResponse>();

    public MutableLiveData<ResultVoid> sendCodeLiveData = new MutableLiveData<ResultVoid>();

    public MutableLiveData<String> errorLiveData = new MutableLiveData<String>();

    private MutableLiveData<String> toastMsg = new MutableLiveData<>("");

    private MutableLiveData<Boolean> agreeChecked = new MutableLiveData<>(false);

    private MutableLiveData<String> sendCodeBtnText = new MutableLiveData<>("发送验证码");

    private boolean isCountdowning = false;
    private int countdown = 60;
    private Timer countdownTimer;

    private final LoginRepository repository = new LoginRepository();
    private final Repository profileRepository = new Repository();

    private final MutableLiveData<ResultUserProfileDto> userProfileLiveData = new MutableLiveData<>();

    public void loginBySms(String phone, String code) {
        String phoneError = repository.validatePhone(phone);
        if (phoneError != null) {
            toastMsg.setValue(phoneError);
            return;
        }
        String codeError = repository.validateCode(code);
        if (codeError != null) {
            toastMsg.setValue(codeError);
            return;
        }
        if (!(agreeChecked.getValue())) {
            toastMsg.setValue("请同意用户协议和隐私政策");
            return;
        }
        Disposable disposable = repository.loginBySms(phone, code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getCode() == ServiceCode.SUCCESS) {
                        repository.saveUserInfo(response.getData());
                        repository.savePassword(response.getData().getGeneratedPassword());
                    } else {
                        toastMsg.setValue("登录失败" + repository.errorCode(response.getCode()));
                    }
                    loginResultLiveData.setValue(response);
                }, error -> {
                    errorLiveData.setValue("登录失败:" + error.getMessage());
                });
        addDisposable(disposable);
    }

    public void sendCode(String phone) {
        String phoneError = repository.validatePhone(phone);
        if (phoneError != null) {
            toastMsg.setValue(phoneError);
            return;
        }

        if (isCountdowning) {
            return;
        }
        Disposable disposable = repository.sendSmsCode(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getCode() == ServiceCode.SUCCESS) {
                        startCountdown();
                    } else {
                        toastMsg.setValue("发送验证码失败" + repository.errorCode(response.getCode()));
                    }
                    sendCodeLiveData.setValue(response);
                }, error -> {
                    errorLiveData.setValue("发送验证码失败：" + error.getMessage());
                });
        addDisposable(disposable);
    }

    private void startCountdown() {
        isCountdowning = true;
        countdown = 60;
        countdownTimer = new Timer();
        countdownTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                countdown--;
                sendCodeBtnText.postValue(countdown + "秒后重新发送");
                if (countdown <= 0) {
                    isCountdowning = false;
                    countdownTimer.cancel();
                    sendCodeBtnText.postValue("发送验证码");
                }
            }
        }, 0, 1000);
    }

    public void setAgreeChecked(Boolean checked) {
        agreeChecked.setValue(checked);
    }

    public MutableLiveData<ResultAuthResponse> getLoginResultLiveData() {
        return loginResultLiveData;
    }

    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<String> getToastMsg() {
        return toastMsg;
    }

    public MutableLiveData<ResultVoid> getSendCodeLiveData() {
        return sendCodeLiveData;
    }

    public MutableLiveData<String> getSendCodeBtnText() {
        return sendCodeBtnText;
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
