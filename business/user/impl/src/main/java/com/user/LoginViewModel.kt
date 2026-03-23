package com.user

import androidx.lifecycle.MutableLiveData
import com.common.base.BaseViewModel
import com.network.NetworkManager
import com.agri.pest.client.api.ServiceCode
import com.agri.pest.client.model.request.LoginRequest
import com.agri.pest.client.model.request.RegisterRequest
import com.agri.pest.client.model.response.ResultAuthResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * 登录注册 ViewModel
 * 使用 AgriPest-Android-SDK 重构
 */
class LoginViewModel : BaseViewModel() {

    // 登录结果状态，使用 SDK 的数据模型
    val loginResultLiveData = MutableLiveData<ResultAuthResponse>()
    val registerResultLiveData = MutableLiveData<ResultAuthResponse>()
    val errorLiveData = MutableLiveData<String>()

    /**
     * 登录方法
     */
    fun login(usernameOrPhone: String, password: String) {
        val request = LoginRequest(
            usernameOrPhone = usernameOrPhone,
            password = password
        )
        
        val disposable = NetworkManager.api.login(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.code == ServiceCode.SUCCESS && response.data != null) {
                    // 登录成功，更新 Token
                    response.data!!.token?.let { NetworkManager.setToken(it) }
                }
                loginResultLiveData.value = response
            }, { error ->
                errorLiveData.value = "登录请求失败: ${error.message}"
            })
        addDisposable(disposable)
    }

    /**
     * 注册方法
     */
    fun register(phone: String, password: String, username: String? = null) {
        val request = RegisterRequest(
            phone = phone,
            password = password,
            username = username
        )
        
        val disposable = NetworkManager.api.register(request)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                if (response.code == ServiceCode.SUCCESS && response.data != null) {
                    // 注册成功，更新 Token
                    response.data?.token?.let { NetworkManager.setToken(it) }
                }
                registerResultLiveData.value = response
            }, { error ->
                errorLiveData.value = "注册请求失败: ${error.message}"
            })
        addDisposable(disposable)
    }
}
