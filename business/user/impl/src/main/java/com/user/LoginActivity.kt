package com.user

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.user.databinding.ActivityLoginBinding

@Route(path = RouterPath.USER_LOGIN_ACTIVITY)
class LoginActivity : BaseActivity<ActivityLoginBinding>() {

    override fun getViewBinding(): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // TODO: Implement login logic
    }

    override fun initData() {
    }
}
