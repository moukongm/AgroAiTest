package com.main

import android.annotation.SuppressLint
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.common.storage.MMKVUtils
import com.common.utils.ImageLoader
import com.common.utils.ImagePickerUtil
import com.common.utils.ToastUtils
import com.common.webview.WebViewActivity
import com.user.UserService
import com.detection.DetectionService
import com.community.CommunityService
import com.main.impl.databinding.ActivityDebugDemoBinding
import com.network.NetworkManager
import com.common.storage.database.AppDatabase
import com.common.storage.database.ChatMessage
import com.common.storage.database.RecognitionRecord
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.UUID

@Route(path = RouterPath.DEBUG_DEMO_ACTIVITY)
class DebugDemoActivity : BaseActivity<ActivityDebugDemoBinding>() {

    private lateinit var imagePickerUtil: ImagePickerUtil

    override fun getViewBinding(): ActivityDebugDemoBinding {
        return ActivityDebugDemoBinding.inflate(layoutInflater)
    }

    @SuppressLint("CheckResult")
    override fun initView() {
        imagePickerUtil = ImagePickerUtil(this) { uri ->
            ImageLoader.load(binding.ivDemo, uri.toString())
            ToastUtils.showShort(this, "图片选择成功")
        }

        binding.btnLogin.setOnClickListener {
            val userService = UserService.api()
            val isLogin = userService.isLogin()
            ToastUtils.showShort(this, "Is Login: $isLogin, User: ${userService.getUserName()}")
            ARouter.getInstance().build(RouterPath.USER_LOGIN_ACTIVITY).navigation()
        }

        binding.btnDetection.setOnClickListener {
            val detectionService = DetectionService.api()
            detectionService.startDetection("test_image_url")
            ARouter.getInstance().build(RouterPath.DETECTION_ACTIVITY).navigation()
        }

        binding.btnCommunity.setOnClickListener {
            val communityService = CommunityService.api()
            val posts = communityService.getLatestPosts(3)
            ToastUtils.showShort(this, "Latest Posts: $posts")
            ARouter.getInstance().build(RouterPath.COMMUNITY_ACTIVITY).navigation()
        }

        binding.btnNetwork.setOnClickListener {
            ToastUtils.showShort(this, "正在请求网络数据...")
            NetworkManager.api
                .getZen()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    ToastUtils.showShort(this, "网络请求成功! 标题: ${response.title}")
                }, { error ->
                    ToastUtils.showShort(this, "网络请求失败: ${error.message}")
                })
        }

        binding.btnMmkv.setOnClickListener {
            val userStorage = MMKVUtils.custom("user_settings")
            val clickCount = userStorage.getInt("demo_click_count", 0) + 1
            userStorage.put("demo_click_count", clickCount)
            ToastUtils.showShort(this, "分场景存储：这是你第 $clickCount 次点击")
        }

        binding.btnWebview.setOnClickListener {
            WebViewActivity.start(this, "https://www.baidu.com", "WebView Demo")
        }

        binding.btnImagePicker.setOnClickListener {
            imagePickerUtil.showImageSourceDialog()
        }
        
        binding.btnDatabase.setOnClickListener {
            Single.fromCallable {
                val db = AppDatabase.getInstance(this)
                // Insert a demo recognition record
                val record = RecognitionRecord(
                    imagePath = "/path/to/demo_image.png",
                    diseaseName = "Demo Disease",
                    confidence = 0.95f,
                    treatment = "Spray demo pesticide"
                )
                db.recognitionDao().insert(record)
                
                // Insert a demo chat message
                val chatMessage = ChatMessage(
                    sessionId = UUID.randomUUID().toString(),
                    role = "user",
                    content = "What disease is this?"
                )
                db.chatDao().insert(chatMessage)
                
                db.recognitionDao().getAllRecords().size
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ size ->
                ToastUtils.showShort(this, "数据库插入成功！当前记录数: $size")
            }, { error ->
                ToastUtils.showShort(this, "数据库操作失败: ${error.message}")
            })
        }

        binding.btnImageLoader.setOnClickListener {
            ImageLoader.loadRounded(binding.ivDemo, "https://github.com/lukecc00/PicImg/blob/main/202308082028393.png", 20f)
            ToastUtils.showShort(this, "正在加载网络图片...")
        }

        binding.btnLocalImageLoader.setOnClickListener {
            ImageLoader.loadLocal(binding.ivDemo, android.R.drawable.sym_def_app_icon)
            ToastUtils.showShort(this, "正在加载本地图片...")
        }
    }

    override fun initData() {
    }
}
