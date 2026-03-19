package com.demo

import android.os.Bundle
import com.common.base.BaseActivity
import com.common.base.BaseFragment
import com.common.utils.ToastUtils
import com.common.utils.setOnDebouncedClickListener
import com.demo.databinding.ActivityFragmentDemoBinding
import com.demo.databinding.FragmentDemoBinding
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * 演示如何动态添加和移除 Fragment
 */
class FragmentDemoActivity : BaseActivity<ActivityFragmentDemoBinding>() {

    private var demoFragment: DemoFragment? = null

    override fun getViewBinding(): ActivityFragmentDemoBinding {
        return ActivityFragmentDemoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.btnAddFragment.setOnDebouncedClickListener {
            if (demoFragment == null) {
                demoFragment = DemoFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, demoFragment!!)
                    .commit()
                ToastUtils.showShort(this, "Fragment 已添加")
            } else {
                ToastUtils.showShort(this, "Fragment 已经存在")
            }
        }

        binding.btnRemoveFragment.setOnDebouncedClickListener {
            if (demoFragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(demoFragment!!)
                    .commit()
                demoFragment = null
                ToastUtils.showShort(this, "Fragment 已移除")
            } else {
                ToastUtils.showShort(this, "当前没有 Fragment 可以移除")
            }
        }
    }

    override fun initData() {
    }
}

/**
 * 一个简单的演示 Fragment
 */
class DemoFragment : BaseFragment<FragmentDemoBinding>() {

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDemoBinding {
        return FragmentDemoBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        binding.tvFragmentContent.setOnDebouncedClickListener {
            ToastUtils.showShort(requireContext(), "点击了 Fragment 内部的内容")
        }
    }

    override fun initData() {
    }
}
