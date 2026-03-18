package com.user

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.user.databinding.ActivityProfileBinding

@Route(path = RouterPath.USER_PROFILE_ACTIVITY)
class ProfileActivity : BaseActivity<ActivityProfileBinding>() {

    override fun getViewBinding(): ActivityProfileBinding {
        return ActivityProfileBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // TODO: Implement user profile
    }

    override fun initData() {
    }
}
