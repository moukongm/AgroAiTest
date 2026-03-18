package com.community

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.router.RouterPath
import com.community.CommunityService

@Route(path = RouterPath.COMMUNITY_SERVICE)
class CommunityServiceImpl : CommunityService {
    override fun getLatestPosts(limit: Int): List<String> {
        return listOf("Post 1", "Post 2", "Post 3").take(limit)
    }

    override fun init(context: Context?) {
    }
}
