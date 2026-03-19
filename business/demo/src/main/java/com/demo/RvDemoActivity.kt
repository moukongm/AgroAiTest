package com.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.uikit.base.BaseBindingMultiAdapter
import com.common.utils.ImageLoader
import com.common.utils.ToastUtils
import com.demo.databinding.ActivityRvDemoBinding
import com.demo.databinding.ItemRvDemoImageBinding
import com.demo.databinding.ItemRvDemoTextBinding

/**
 * ============================================
 * 1. 定义数据实体，必须实现 MultiItemEntity 接口
 * ============================================
 */
data class DemoMultiItem(
    override val itemType: Int, // BRVAH 框架需要的类型标识
    val textContent: String = "",
    val imageUrl: String = ""
) : MultiItemEntity {
    companion object {
        const val TYPE_TEXT_ONLY = 1
        const val TYPE_IMAGE_TEXT = 2
    }
}

/**
 * ============================================
 * 2. 编写多类型 Adapter，继承 BaseBindingMultiAdapter
 * ============================================
 */
class DemoMultiAdapter : BaseBindingMultiAdapter<DemoMultiItem>() {
    init {
        // 关键步：将 ItemType 和对应的 ViewBinding.inflate 绑定
        addItemBinding(DemoMultiItem.TYPE_TEXT_ONLY, ItemRvDemoTextBinding::inflate)
        addItemBinding(DemoMultiItem.TYPE_IMAGE_TEXT, ItemRvDemoImageBinding::inflate)
    }

    override fun convert(binding: ViewBinding, item: DemoMultiItem, itemType: Int, position: Int) {
        // 根据类型强转为对应的 Binding 进行 UI 更新
        when (itemType) {
            DemoMultiItem.TYPE_TEXT_ONLY -> {
                val textBinding = binding as ItemRvDemoTextBinding
                textBinding.tvTitle.text = item.textContent
            }
            DemoMultiItem.TYPE_IMAGE_TEXT -> {
                val imgBinding = binding as ItemRvDemoImageBinding
                imgBinding.tvDesc.text = item.textContent
                // 使用现成的图片加载工具加载默认图标作为演示
                ImageLoader.loadLocal(imgBinding.ivCover, android.R.drawable.ic_menu_gallery)
            }
        }
    }
}

/**
 * ============================================
 * 3. 页面实现
 * ============================================
 */
class RvDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRvDemoBinding
    private val mAdapter = DemoMultiAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRvDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 初始化 RecyclerView
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = mAdapter

        // 2. 模拟请求并设置混合数据
        val mockData = mutableListOf<DemoMultiItem>()
        mockData.add(DemoMultiItem(DemoMultiItem.TYPE_TEXT_ONLY, textContent = "这是一个纯文本的标题项"))
        mockData.add(DemoMultiItem(DemoMultiItem.TYPE_IMAGE_TEXT, textContent = "这是一条带图片的图文卡片 1"))
        mockData.add(DemoMultiItem(DemoMultiItem.TYPE_IMAGE_TEXT, textContent = "这是一条带图片的图文卡片 2"))
        mockData.add(DemoMultiItem(DemoMultiItem.TYPE_TEXT_ONLY, textContent = "这是中间插入的另一个文本项"))
        mockData.add(DemoMultiItem(DemoMultiItem.TYPE_IMAGE_TEXT, textContent = "这是一条带图片的图文卡片 3"))
        
        // 设置动画：滑动进入
        mAdapter.setAnimationWithDefault(com.chad.library.adapter.base.BaseQuickAdapter.AnimationType.SlideInBottom)
        
        mAdapter.setList(mockData)

        // 3. 设置点击事件
        mAdapter.setOnItemClickListener { _, _, position ->
            val item = mAdapter.getItem(position)
            ToastUtils.showShort(this, "你点击了：${item.textContent}")
        }
    }
}
