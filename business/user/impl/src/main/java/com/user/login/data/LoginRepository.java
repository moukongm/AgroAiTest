package com.user.login.data;

import com.agri.pest.client.api.ServiceCode;
import com.agri.pest.client.model.request.LoginRequest;
import com.agri.pest.client.model.request.RefreshTokenRequest;
import com.agri.pest.client.model.request.RegisterRequest;
import com.agri.pest.client.model.request.SendSmsRequest;
import com.agri.pest.client.model.request.SmsLoginRequest;
import com.agri.pest.client.model.response.AuthResponse;
import com.agri.pest.client.model.response.ResultAuthResponse;
import com.agri.pest.client.model.response.ResultVoid;
import com.network.NetworkManager;

import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class LoginRepository {

    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    public boolean ans = true;

    public Single<ResultAuthResponse> register(String phone, String password, String username) {
        RegisterRequest request = new RegisterRequest(phone, password, username);
        return NetworkManager.INSTANCE.getApi().register(request);
    }

    public Single<ResultAuthResponse> login(String phone, String password) {
        LoginRequest request = new LoginRequest(phone, password);
        return NetworkManager.INSTANCE.getApi().login(request);
    }

    public Single<ResultAuthResponse> loginBySms(String phone, String code) {
        SmsLoginRequest request = new SmsLoginRequest(phone, code);
        return NetworkManager.INSTANCE.getApi().loginBySms(request);
    }

    public Single<ResultVoid> sendSmsCode(String phone) {
        SendSmsRequest request = new SendSmsRequest(phone);
        return NetworkManager.INSTANCE.getApi().sendSmsCode(request);
    }

    public Single<ResultAuthResponse> refresh() {
        RefreshTokenRequest request = new RefreshTokenRequest(UserStorageConstant.getRefreshToken());
        return NetworkManager.INSTANCE.getApi().refresh(request);
    }

    public boolean refreshToken() {
        Disposable disposable = refresh()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getCode() == ServiceCode.SUCCESS) {
                        saveUserInfo(response.getData());
                        ans = true;
                    } else {
                        ans = false;
                    }
                }, error -> {
                    ans = false;
                });
        return ans;
    }

    public String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "请输入手机号";
        }
        if (!Pattern.matches(PHONE_PATTERN, phone.trim())) {
            return "请输入正确的手机号";
        }
        return null;
    }


    public String validateCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "请输入验证码";
        }
        return null;
    }

    public String validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return "请输入密码";
        }
        return null;
    }


    public String errorCode(int code) {
        switch (code) {
            case ServiceCode.SUCCESS:
                return "请求成功";
            case ServiceCode.ERROR_400:
                return "客户端提交的参数不符合要求（例如手机号格式错误）";
            case ServiceCode.INTERNAL_SERVER_ERROR:
                return "服务器内部发生未捕获的异常";
            case ServiceCode.INVALID_REQUEST_PARAMETERS:
                return "请求参数缺失或不合法";
            case ServiceCode.USERNAME_ALREADY_EXISTS:
                return "注册时用户名已被占用";
            case ServiceCode.PHONE_NUMBER_ALREADY_EXISTS:
                return "注册时手机号已被绑定";
            case ServiceCode.USER_NOT_FOUND:
                return "登录或刷新时未找到该用户";
            case ServiceCode.INVALID_USERNAME_OR_PASSWORD:
                return "账号或密码错误";
            case ServiceCode.INVALID_TOKEN_OR_TOKEN_EXPIRED:
                return "Token无效、伪造、版本号过期或已超时";
            case ServiceCode.FAILED_TO_SEND_SMS_CODE:
                return "短信验证码发送失败（第三方服务异常等）";
            case ServiceCode.INVALID_OR_EXPIRED_SMS_CODE:
                return "短信验证码填写错误或已过期";
            case ServiceCode.USERNAME_MUST_BE_BETWEEN_2_AND_15_CHARACTERS:
                return "用户名长度不符合规范";
            case ServiceCode.ACCESS_DENIED:
                return "权限不足（如非管理员尝试访问后台）";
            case ServiceCode.POST_NOT_FOUND:
                return "请求查看或操作的帖子不存在";
            case ServiceCode.COMMENT_NOT_FOUND:
                return "请求操作的评论不存在";
            case ServiceCode.POST_ALREADY_LIKED:
                return "用户已经对该帖子点过赞，不能重复点赞";
            case ServiceCode.POST_ALREADY_FAVORITED:
                return "用户已经收藏过该帖子，不能重复收藏";
            case ServiceCode.NO_PERMISSION_TO_PERFORM_THIS_ACTION:
                return "越权操作（例如尝试删除别人的帖子）";
            case ServiceCode.CROP_NOT_FOUND:
                return "请求操作的作物不存在或不属于当前用户";
            case ServiceCode.MEDICATION_PLAN_NOT_FOUND:
                return "用药打卡计划不存在";
            case ServiceCode.DETECTION_RECORD_NOT_FOUND:
                return "识别记录不存在";
            case ServiceCode.AI_SERVICE_IS_CURRENTLY_UNAVAILABLE:
                return "大模型识别服务不可用、超时或配置错误";
            case ServiceCode.FILE_UPLOAD_FAILED:
                return "文件上传到存储服务失败";
            case ServiceCode.FILE_SIZE_EXCEEDS_THE_MAXIMUM_LIMIT:
                return "文件大小超过了服务器限制（通常为 10MB）";
            case ServiceCode.INVALID_FILE_TYPE:
                return "文件格式不允许（例如要求图片却上传了文本文件）";
            case ServiceCode.FAILED_TO_INITIALIZE_STORAGE_SERVICE:
                return "存储服务（MinIO）初始化失败";
            case ServiceCode.MESSAGE_NOTIFICATION_NOT_FOUND:
                return "消息通知不存在";
            case ServiceCode.ADMIN_BROADCAST_RECORD_NOT_FOUND:
                return "管理员发布记录不存在";
            default:
                return "未知错误码: " + code;
        }
    }

    public void saveUserInfo(AuthResponse data) {
        if (data == null) return;

        NetworkManager.INSTANCE.setToken(data.getToken());
        UserStorageConstant.saveToken(data.getToken());
        UserStorageConstant.saveUserId(data.getUserId());
        UserStorageConstant.saveUserName(data.getUsername());
        UserStorageConstant.saveRefreshToken(data.getRefreshToken());
        UserStorageConstant.saveExpiresIn(data.getExpiresIn());
    }

    public void savePassword(String password) {
        UserStorageConstant.savePassword(password);
    }
}
