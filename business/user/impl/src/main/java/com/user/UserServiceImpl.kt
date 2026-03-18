package com.user

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.router.RouterPath
import com.user.UserService

@Route(path = RouterPath.USER_SERVICE)
class UserServiceImpl : UserService {
    override fun isLogin(): Boolean {
        return false // Mock implementation
    }

    override fun getUserName(): String? {
        return "Guest"
    }

    override fun init(context: Context?) {
    }
}
