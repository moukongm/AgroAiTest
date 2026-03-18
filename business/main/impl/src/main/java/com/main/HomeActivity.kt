package com.main

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
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
import com.main.impl.databinding.ActivityHomeBinding
import com.network.NetworkManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

@Route(path = RouterPath.MAIN_ACTIVITY)
class HomeActivity : BaseActivity<ActivityHomeBinding>() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var imagePickerUtil: ImagePickerUtil

    override fun getViewBinding(): ActivityHomeBinding {
        return ActivityHomeBinding.inflate(layoutInflater)
    }

    @SuppressLint("CheckResult")
    override fun initView() {
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        imagePickerUtil = ImagePickerUtil(this) { uri ->
            // 这里获取到选择或拍照后的图片 URI
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
            // 获取一个名为 "user_settings" 的专属存储场景
            val userStorage = MMKVUtils.custom("user_settings")
            val clickCount = userStorage.getInt("demo_click_count", 0) + 1
            userStorage.put("demo_click_count", clickCount)
            ToastUtils.showShort(this, "分场景存储：这是你第 $clickCount 次点击")
            
            // userStorage.clear() // 这样清除只会清除 "user_settings" 里面的数据，不影响全局或其他场景
        }

        binding.btnWebview.setOnClickListener {
            WebViewActivity.start(this, "https://www.baidu.com", "WebView Demo")
        }

        binding.btnImagePicker.setOnClickListener {
            imagePickerUtil.showImageSourceDialog()
        }

        binding.btnImageLoader.setOnClickListener {
            // 使用一个更稳定、加载速度更快的图片链接测试 (Coil 默认不支持 svg，换成稳定的 png)
            ImageLoader.loadRounded(binding.ivDemo, "https://github.com/lukecc00/PicImg/blob/main/202308082028393.png", 20f)
            ToastUtils.showShort(this, "正在加载网络图片...")
        }

        binding.btnLocalImageLoader.setOnClickListener {
            // using android internal res to avoid R unresolved reference issue
            ImageLoader.loadLocal(binding.ivDemo, android.R.drawable.sym_def_app_icon)
            ToastUtils.showShort(this, "正在加载本地图片...")
        }

        // Load HomeFragment
        supportFragmentManager.beginTransaction()
            .replace(com.main.impl.R.id.fragment_container, HomeFragment())
            .commit()
    }

    override fun initData() {
        // Handle data observation or initialization here via viewModel
    }
}
