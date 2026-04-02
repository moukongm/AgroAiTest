package com.agroai

import coil.ImageLoader
import coil.ImageLoaderFactory
import com.alibaba.android.arouter.launcher.ARouter
import com.common.BaseApplication
import com.common.storage.MMKVUtils
import com.network.NetworkManager
import android.content.pm.ApplicationInfo

class App : BaseApplication(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()

        // 1. 初始化 ARouter
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (isDebug) {
            ARouter.openLog()
            ARouter.openDebug()
        }
        ARouter.init(this)

        // 2. 初始化 MMKV
        MMKVUtils.init(this)
    }

    /**
     * 配置全局的 Coil ImageLoader，让其使用忽略 SSL 校验的 OkHttpClient
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .okHttpClient { NetworkManager.okHttpClient }
            .allowHardware(false)
            .build()
    }
}
