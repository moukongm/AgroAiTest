package com.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.common.base.BaseFragment
import com.main.impl.databinding.FragmentHomeBinding

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        // Initialize fragment views here
    }

    override fun initData() {
        // Initialize fragment data here
    }
}