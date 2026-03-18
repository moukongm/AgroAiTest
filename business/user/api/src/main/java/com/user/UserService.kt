package com.user

import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter

interface UserService : IProvider {
    fun isLogin(): Boolean
    fun getUserName(): String?
    
    companion object {
        fun api(): UserService = ARouter.getInstance().navigation(UserService::class.java)
    }
}
