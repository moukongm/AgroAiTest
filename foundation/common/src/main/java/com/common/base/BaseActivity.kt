package com.common.base

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.common.widget.LoadingDialog

/**
 * Activity 基类
 * 封装了 ViewBinding 的初始化和其他通用逻辑
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected lateinit var binding: VB
    private var loadingDialog: LoadingDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initImmersiveStatusBar()
        binding = getViewBinding()
        setContentView(binding.root)
        initView()
        initData()
    }

    /**
     * 初始化沉浸式状态栏
     */
    private fun initImmersiveStatusBar() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        // 设置状态栏文字颜色：浅色主题显示黑色文字，深色主题保持默认（白色）
        var systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        if (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK != android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        window.decorView.systemUiVisibility = systemUiVisibility
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }

    /**
     * 显示加载弹窗
     */
    fun showLoading(message: String = "加载中...") {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(this)
        }
        loadingDialog?.setMessage(message)
        if (loadingDialog?.isShowing == false) {
            loadingDialog?.show()
        }
    }

    /**
     * 隐藏加载弹窗
     */
    fun hideLoading() {
        if (loadingDialog?.isShowing == true) {
            loadingDialog?.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hideLoading()
    }

    /**
     * 获取 ViewBinding 实例
     */
    abstract fun getViewBinding(): VB

    /**
     * 初始化视图
     */
    abstract fun initView()

    /**
     * 初始化数据
     */
    abstract fun initData()
}
