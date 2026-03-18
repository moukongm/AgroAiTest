package com.agroai

import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.agroai.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // Automatically jump to HomeActivity
        ARouter.getInstance().build(RouterPath.MAIN_ACTIVITY).navigation()
        finish()
    }

    override fun initData() {
        // No data initialization needed for a splash/launch screen
    }
}