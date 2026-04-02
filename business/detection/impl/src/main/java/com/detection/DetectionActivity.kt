package com.detection

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.detection.databinding.ActivityDetectionBinding

@Route(path = RouterPath.DETECTION_ACTIVITY)
class DetectionActivity : BaseActivity<ActivityDetectionBinding>() {

    override fun getViewBinding(): ActivityDetectionBinding {
        return ActivityDetectionBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // TODO: Implement camera and detection logic
    }

    override fun initData() {
    }
}
