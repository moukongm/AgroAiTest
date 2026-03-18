package com.detection

import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter

interface DetectionService : IProvider {
    fun startDetection(imageUrl: String)
    
    companion object {
        fun api(): DetectionService = ARouter.getInstance().navigation(DetectionService::class.java)
    }
}
