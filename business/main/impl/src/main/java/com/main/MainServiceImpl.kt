package com.main

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.router.RouterPath
import com.main.MainService

@Route(path = RouterPath.MAIN_SERVICE)
class MainServiceImpl : MainService {
    override fun getMainInfo(): String {
        return "Info from Main Module"
    }

    override fun init(context: Context?) {
        // Initialize if needed
    }
}
