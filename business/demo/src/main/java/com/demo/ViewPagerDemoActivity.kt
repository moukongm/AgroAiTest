package com.demo

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.common.base.BaseActivity
import com.demo.databinding.ActivityViewpagerDemoBinding
import com.uikit.base.BaseFragmentPagerAdapter

/**
 * 演示如何使用封装好的 BaseFragmentPagerAdapter 搭配 ViewPager2
 */
class ViewPagerDemoActivity : BaseActivity<ActivityViewpagerDemoBinding>() {

    override fun getViewBinding(): ActivityViewpagerDemoBinding {
        return ActivityViewpagerDemoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        // 1. 初始化我们封装的 Adapter
        val pagerAdapter = BaseFragmentPagerAdapter(this)

        // 2. 准备 Fragment 数据
        val fragments = listOf(
            ColorFragment.newInstance("第一页", Color.parseColor("#FFCDD2")),
            ColorFragment.newInstance("第二页", Color.parseColor("#C8E6C9")),
            ColorFragment.newInstance("第三页", Color.parseColor("#BBDEFB")),
            ColorFragment.newInstance("第四页", Color.parseColor("#FFF9C4"))
        )

        // 3. 赋值并绑定
        pagerAdapter.setFragments(fragments)
        binding.viewPager.adapter = pagerAdapter
        
        // 预加载相邻的一页，避免滑动卡顿（视业务需求而定）
        binding.viewPager.offscreenPageLimit = 1
    }

    override fun initData() {
    }
}

/**
 * 一个用于演示的简单 Fragment
 */
class ColorFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_COLOR = "color"

        fun newInstance(title: String, colorInt: Int): ColorFragment {
            val fragment = ColorFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putInt(ARG_COLOR, colorInt)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val title = arguments?.getString(ARG_TITLE) ?: "Unknown"
        val colorInt = arguments?.getInt(ARG_COLOR) ?: Color.WHITE

        // 纯代码构建一个简单的居中 TextView 作为视图
        return TextView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            text = title
            textSize = 24f
            gravity = Gravity.CENTER
            setBackgroundColor(colorInt)
            setTextColor(Color.DKGRAY)
        }
    }
}
