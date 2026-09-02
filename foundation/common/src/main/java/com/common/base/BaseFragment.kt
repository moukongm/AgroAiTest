package com.common.base

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.alibaba.android.arouter.BuildConfig
import com.common.NavigationController
import com.common.utils.HotAreaHelper
import com.common.utils.LogUtils
import com.common.widget.HotAreaBorderView

/**
 * Fragment 基类
 * 封装了 ViewBinding 的初始化和其他通用逻辑
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    private var _binding: VB? = null
    protected val binding get() = _binding!!
    private var navController: NavigationController? = null

    private val hotAreaHighlights = mutableListOf<HotAreaBorderView>()

    /**
     * 安全获取 ViewBinding，在 onDestroyView 后可能为 null
     * @return ViewBinding 实例或 null
     */
    protected fun getBindingSafe(): VB? = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = getViewBinding(inflater, container)
        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationController) {
            navController = context
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        view.post {
            performHotAreaDetection()
        }
    }

    override fun onDestroyView() {
        clearHotAreaHighlights()
        (activity as? BaseActivity<*>)?.setHotAreaEnabled(true)
        _binding = null
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        LogUtils.d("HotArea", "Fragment onResume: ${this::class.java.simpleName}")
        (activity as? BaseActivity<*>)?.setHotAreaEnabled(false)
        binding.root.post {
            performHotAreaDetection()
        }
    }

    override fun onPause() {
        super.onPause()
        LogUtils.d("HotArea", "Fragment onPause: ${this::class.java.simpleName}")
        clearHotAreaHighlights()
        (activity as? BaseActivity<*>)?.setHotAreaEnabled(true)
    }

    private fun performHotAreaDetection() {
        val view = getView() as? ViewGroup ?: return
        if (!isAdded) return
        clearHotAreaHighlights()
        val highlights = HotAreaHelper.highlight(view, view)
        hotAreaHighlights.addAll(highlights)
    }

    private fun showHotAreaHighlights() {
        if (isAdded) {
            hotAreaHighlights.forEach { it.show() }
        }
    }
    private fun hideHotAreaHighlights() {
        hotAreaHighlights.forEach { it.hide() }
    }

    protected fun clearHotAreaHighlights() {
        hotAreaHighlights.forEach { it.detach() }
        hotAreaHighlights.clear()
    }

    protected fun hideBottomNav() {
        navController?.hideBottomNavigation()
    }

    protected fun showBottomNav() {
        navController?.showBottomNavigation()
    }
    /**
     * 显示加载弹窗 (调用宿主 Activity 的方法)
     */
    fun showLoading(message: String = "加载中...") {
        val activity = activity
        if (activity is BaseActivity<*>) {
            activity.showLoading(message)
        }
    }

    /**
     * 隐藏加载弹窗
     */
    fun hideLoading() {
        val activity = activity
        if (activity is BaseActivity<*>) {
            activity.hideLoading()
        }
    }

    /**
     * 获取 ViewBinding 实例
     */
    abstract fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * 初始化视图
     */
    abstract fun initView()

    /**
     * 初始化数据
     */
    abstract fun initData()
}
