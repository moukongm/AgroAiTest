package com.common

import android.app.Application
import com.alibaba.android.arouter.launcher.ARouter
import com.common.storage.MMKVUtils

/**
 * 基础 Application
 * 负责全局初始化，如 ARouter, MMKV 等
 */
open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initARouter()
        initMMKV()
    }

    /**
     * 初始化 ARouter 路由框架
     */
    private fun initARouter() {
        // TODO: 建议根据 BuildConfig.DEBUG 判断
        if (true) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)
    }

    /**
     * 初始化 MMKV 存储
     */
    private fun initMMKV() {
        MMKVUtils.init(this)
    }
}
