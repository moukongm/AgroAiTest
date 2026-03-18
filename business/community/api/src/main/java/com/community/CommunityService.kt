package com.community

import com.alibaba.android.arouter.facade.template.IProvider
import com.alibaba.android.arouter.launcher.ARouter

interface CommunityService : IProvider {
    fun getLatestPosts(limit: Int): List<String>
    
    companion object {
        fun api(): CommunityService = ARouter.getInstance().navigation(CommunityService::class.java)
    }
}
