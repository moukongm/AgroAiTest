package com.demo

import android.annotation.SuppressLint
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.common.storage.MMKVUtils
import com.common.utils.FileUtils
import com.common.utils.ImageLoader
import com.common.utils.ImagePickerUtil
import com.common.utils.ToastUtils
import com.common.webview.WebViewActivity
import com.user.UserService
import com.detection.DetectionService
import com.demo.databinding.ActivityDebugDemoBinding
import com.network.NetworkManager
import com.agri.pest.client.api.ServiceCode
import com.common.storage.database.AppDatabase
import com.common.storage.database.ChatMessage
import com.common.storage.database.RecognitionRecord
import com.common.utils.LogUtils
import com.common.utils.ThreadUtils
import com.common.utils.setOnDebouncedClickListener
import com.common.utils.toggleVisibility
import com.common.utils.visible
import com.common.utils.gone
import com.common.utils.PermissionUtils
import androidx.lifecycle.MutableLiveData
import com.common.utils.observeNonNull
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.UUID
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@Route(path = RouterPath.DEBUG_DEMO_ACTIVITY)
class DebugDemoActivity : BaseActivity<ActivityDebugDemoBinding>() {

    private lateinit var imagePickerUtil: ImagePickerUtil
    private var selectedImageUri: Uri? = null
    private var lastUploadedImageUrl: String? = null
    
    // 用于演示 LiveDataExt
    private val demoLiveData = MutableLiveData<String>()

    override fun getViewBinding(): ActivityDebugDemoBinding {
        return ActivityDebugDemoBinding.inflate(layoutInflater)
    }

    @SuppressLint("CheckResult")
    override fun initView() {
        imagePickerUtil = ImagePickerUtil(this) { uri ->
            selectedImageUri = uri
            ImageLoader.load(binding.ivDemo, uri.toString())
            ToastUtils.showShort(this, "图片选择成功")
        }

        binding.btnUploadFile.setOnDebouncedClickListener {
            val uri = selectedImageUri
            if (uri == null) {
                ToastUtils.showShort(this, "请先点击下方的图片选择器选择一张图片")
                return@setOnDebouncedClickListener
            }

            uploadImage(uri)
        }
        
        // 演示 LiveData 扩展，监听非空数据
        demoLiveData.observeNonNull(this) { value ->
            ToastUtils.showShort(this, "LiveData 收到非空数据: $value")
        }

        binding.btnLogin.setOnDebouncedClickListener {
            val userService = UserService.api()
            val isLogin = userService.isLogin()
            ToastUtils.showShort(this, "Is Login: $isLogin, User: ${userService.getUserName()}")
            ARouter.getInstance().build(RouterPath.USER_PROFILE_ACTIVITY).navigation()
        }

        binding.btnDetection.setOnDebouncedClickListener {
            val detectionService = DetectionService.api()
            detectionService.startDetection("test_image_url")
            ARouter.getInstance().build(RouterPath.DETECTION_ACTIVITY).navigation()
        }

        binding.btnCommunity.setOnDebouncedClickListener {
            ARouter.getInstance().build(RouterPath.COMMUNITY_ACTIVITY).navigation()
        }

        binding.btnNetwork.setOnDebouncedClickListener {
            ToastUtils.showShort(this, "正在请求健康检查接口...")
            NetworkManager.api
                .health()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    ToastUtils.showShort(this, "健康检查成功! 状态: ${response.data}")
                }, { error ->
                    ToastUtils.showShort(this, "健康检查失败: ${error.message}")
                })
        }

        binding.btnSpeech.setOnDebouncedClickListener {
            ARouter.getInstance().build(RouterPath.SPEECH_DEMO_ACTIVITY).navigation()
        }

        binding.btnMmkv.setOnDebouncedClickListener {
            val userStorage = MMKVUtils.custom("user_settings")
            val clickCount = userStorage.getInt("demo_click_count", 0) + 1
            userStorage.put("demo_click_count", clickCount)
            ToastUtils.showShort(this, "分场景存储：这是你第 $clickCount 次点击")
        }

        binding.btnWebview.setOnDebouncedClickListener {
            WebViewActivity.start(this, "https://www.baidu.com", "WebView Demo")
        }

        binding.btnImagePicker.setOnDebouncedClickListener {
            imagePickerUtil.showImageSourceDialog()
        }
        
        binding.btnDatabase.setOnDebouncedClickListener {
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

        binding.btnThread.setOnDebouncedClickListener {
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
        
        binding.btnPermission.setOnDebouncedClickListener {
            PermissionUtils.request(
                this,
                listOf(android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                onGranted = {
                    ToastUtils.showShort(this, "相机与存储权限申请成功！")
                },
                onDenied = { deniedList ->
                    ToastUtils.showShort(this, "被拒绝的权限: $deniedList")
                }
            )
        }

        binding.btnLog.setOnDebouncedClickListener {
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

        binding.btnRvDemo.setOnDebouncedClickListener {
            val intent = android.content.Intent(this, RvDemoActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnLivedataDemo.setOnDebouncedClickListener {
            // 发送数据，上面 observeNonNull 处会收到回调
            demoLiveData.value = "Hello LiveData at ${System.currentTimeMillis()}"
        }

        binding.btnFragmentDemo.setOnDebouncedClickListener {
            val intent = android.content.Intent(this, FragmentDemoActivity::class.java)
            startActivity(intent)
        }

        binding.btnViewpagerDemo.setOnDebouncedClickListener {
            val intent = android.content.Intent(this, ViewPagerDemoActivity::class.java)
            startActivity(intent)
        }

        binding.btnLoadingDemo.setOnDebouncedClickListener {
            // 显示 Loading
            showLoading("正在加载数据，请稍候...")
            
            // 模拟一个 3 秒的耗时任务
            ThreadUtils.executeDelayed({
                ThreadUtils.runOnUiThread {
                    // 隐藏 Loading
                    hideLoading()
                    ToastUtils.showShort(this, "加载完成！")
                }
            }, 3000)
        }

        binding.btnImageLoader.setOnDebouncedClickListener {
            val url = lastUploadedImageUrl ?: "https://github.com/lukecc00/PicImg/blob/main/202308082028393.png"
            ImageLoader.loadRounded(binding.ivDemo, url, 20f)
            val msg = if (lastUploadedImageUrl != null) "正在加载刚才上传的图片..." else "加载默认网络图片..."
            ToastUtils.showShort(this, msg)
        }

        binding.btnLocalImageLoader.setOnDebouncedClickListener {
            ImageLoader.loadLocal(binding.ivDemo, android.R.drawable.sym_def_app_icon)
            ToastUtils.showShort(this, "正在加载本地图片...")
        }
    }

    override fun initData() {
    }

    /**
     * 文件上传 Demo
     */
    private fun uploadImage(uri: Uri) {
        showLoading("正在上传图片...")
        
        // 1. 将 Uri 转换为 File (通常在后台线程执行，避免阻塞 UI)
        ThreadUtils.executeByIo {
            val file = FileUtils.uriToFile(this, uri)
            if (file == null || !file.exists()) {
                ThreadUtils.runOnUiThread {
                    hideLoading()
                    ToastUtils.showShort(this, "文件解析失败")
                }
                return@executeByIo
            }

            // 2. 构造 MultipartBody.Part
            // 注意: "file" 是后端约定的字段名，必须与接口定义一致
            val requestFile = file.asRequestBody(FileUtils.getMimeType(file).toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestFile)

            // 3. 调用 SDK 接口上传
             val uploadObservable = NetworkManager.api
                .uploadFile(part, "uploads/demo/") // prefix 为可选的前缀
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    hideLoading()
                    if (response.code == ServiceCode.SUCCESS) {
                        lastUploadedImageUrl = response.data
                        LogUtils.d("上传成功: ${response.data}")
                        ToastUtils.showShort(this, "上传成功！点击下方【加载网络图片】即可查看")
                    } else {
                        ToastUtils.showShort(this, "上传失败: ${response.message}")
                    }
                }, { error ->
                    hideLoading()
                    LogUtils.e("上传异常", error)
                    ToastUtils.showShort(this, "上传异常: ${error.message}")
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 可选：清理上传时产生的临时文件
    }
}
