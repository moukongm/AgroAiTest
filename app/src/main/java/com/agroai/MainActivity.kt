package com.agroai

import android.os.Bundle
import androidx.fragment.app.commit
import com.agroai.databinding.ActivityMainBinding
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.main.ui.page.HomeFragment

@Route(path = RouterPath.MAIN_ACTIVITY)
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun getViewBinding(): ActivityMainBinding {
        return ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(binding.fragmentContainer.id, HomeFragment())
            }
        }
    }

    override fun initView() {
    }

    override fun initData() {
    }
}
