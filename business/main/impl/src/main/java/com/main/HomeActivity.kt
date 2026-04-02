package com.main

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.main.impl.databinding.ActivityHomeBinding

//@Route(path = RouterPath.MAIN_ACTIVITY)
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private lateinit var viewModel: HomeViewModel

    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    override fun initView() {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        binding.btnGoDebug.setOnClickListener {
            ARouter.getInstance().build(RouterPath.DEBUG_DEMO_ACTIVITY).navigation()
        }

        // Load HomeFragment
        supportFragmentManager.beginTransaction()
            .replace(com.main.impl.R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun initData() {
        // Handle data observation or initialization here via viewModel
    }
}
