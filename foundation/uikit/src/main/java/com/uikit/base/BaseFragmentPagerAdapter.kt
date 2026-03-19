package com.uikit.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * 极简 ViewPager2 + Fragment 适配器基类
 * 
 * 解决了常见的 Fragment 重复创建、滑动冲突问题。
 * 支持传入 Activity 或者 Fragment 作为宿主生命周期管理者。
 */
class BaseFragmentPagerAdapter : FragmentStateAdapter {

    private val fragments: MutableList<Fragment> = mutableListOf()

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)
    
    constructor(fragment: Fragment) : super(fragment)
    
    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle) : super(fragmentManager, lifecycle)

    /**
     * 设置或刷新所有的 Fragment
     */
    fun setFragments(newFragments: List<Fragment>) {
        fragments.clear()
        fragments.addAll(newFragments)
        notifyDataSetChanged()
    }

    /**
     * 追加 Fragment
     */
    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
        notifyItemInserted(fragments.size - 1)
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}
