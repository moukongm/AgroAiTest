package com.community

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.community.databinding.ActivityCommunityBinding

@Route(path = RouterPath.COMMUNITY_ACTIVITY)
class CommunityActivity : BaseActivity<ActivityCommunityBinding>() {

    override fun getViewBinding(): ActivityCommunityBinding {
        return ActivityCommunityBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // TODO: Implement community feature
    }

    override fun initData() {
    }
}
