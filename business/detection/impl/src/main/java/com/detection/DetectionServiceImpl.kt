package com.detection

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.router.RouterPath
import com.detection.DetectionService

@Route(path = RouterPath.DETECTION_SERVICE)
class DetectionServiceImpl : DetectionService {
    override fun startDetection(imageUrl: String) {
        // Implementation logic
        println("Starting detection for $imageUrl")
    }

    override fun init(context: Context?) {
    }
}
