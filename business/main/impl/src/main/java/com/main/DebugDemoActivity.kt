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
import com.common.utils.LogUtils
import com.common.utils.ThreadUtils
import com.common.utils.setOnDebouncedClickListener
import com.common.utils.toggleVisibility
import com.common.utils.visible
import com.common.utils.gone
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

        binding.btnThread.setOnClickListener {
            // 1. IO 密集型任务示例
            ThreadUtils.executeByIo {
                val threadName = Thread.currentThread().name
                ThreadUtils.runOnUiThread {
                    ToastUtils.showShort(this, "IO 任务执行在：$threadName")
                }
            }

            // 2. CPU 密集型任务示例
            ThreadUtils.executeByCpu {
                var result = 0L
                for (i in 0..10000) { result += i }
                val threadName = Thread.currentThread().name
                
                ThreadUtils.runOnUiThreadDelayed({
                    ToastUtils.showShort(this, "CPU 任务执行在：$threadName, 结果:$result")
                }, 1000) // 延迟1秒在主线程弹窗
            }

            // 3. 周期性调度任务示例
            ThreadUtils.executeDelayed({
                ThreadUtils.runOnUiThread {
                    ToastUtils.showShort(this, "这是一个延迟 2 秒后执行的调度任务")
                }
            }, 2000)
        }

        binding.btnLog.setOnClickListener {
            LogUtils.v("这是一条 Verbose 日志 (自动生成 TAG)")
            LogUtils.d("这是一条 Debug 日志 (自动生成 TAG)")
            LogUtils.i("CustomTag", "这是一条指定 TAG 的 Info 日志")
            LogUtils.w("这是一条 Warn 日志", Exception("模拟的一个警告异常"))
            LogUtils.e("这是一条 Error 日志", RuntimeException("模拟的一个严重异常"))
            
            // 模拟超长日志测试
            val longString = buildString {
                for (i in 1..500) append("这是超长日志填充文本内容 $i;")
            }
            LogUtils.d("LongLogTest", longString)
            ToastUtils.showShort(this, "已输出各类日志，请查看 Logcat")
        }
        
        binding.btnViewExt.setOnDebouncedClickListener {
            ToastUtils.showShort(this, "防抖点击触发！你可以快速连点试试")
            // 切换下方 iv_demo 的可见性
            binding.ivDemo.toggleVisibility()
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
