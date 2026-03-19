package com.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
class FragmentDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFragmentDemoBinding
    private var demoFragment: DemoFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFragmentDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
}

/**
 * 一个简单的演示 Fragment
 */
class DemoFragment : Fragment() {
    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDemoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvFragmentContent.setOnDebouncedClickListener {
            ToastUtils.showShort(requireContext(), "点击了 Fragment 内部的内容")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
