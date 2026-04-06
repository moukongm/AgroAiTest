package com.agroai

import coil.ImageLoader
import coil.ImageLoaderFactory
import com.amap.api.location.AMapLocationClient
import com.common.BaseApplication
import com.network.NetworkManager

class App : BaseApplication(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // 注意: ARouter 已在 BaseApplication 中初始化，无需重复初始化
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
