package com.uikit.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.chad.library.adapter.base.viewholder.BaseViewHolder

/**
 * 基于 ViewBinding 的多类型 (Multi-Type) RecyclerView Adapter 基类
 * 
 * 适用于一个列表中存在多种不同 UI 样式的场景（例如：首页的 Banner、通知、普通商品列表混排）。
 *
 * @param T 数据模型，必须实现 MultiItemEntity 接口来提供 itemType
 */
abstract class BaseBindingMultiAdapter<T : MultiItemEntity>(
    data: MutableList<T>? = null
) : BaseMultiItemQuickAdapter<T, BaseViewHolder>(data) {

    // 存储注册的 itemType 与对应的 ViewBinding 反射生成方法的映射关系
    private val bindingInflateMap = mutableMapOf<Int, (LayoutInflater, ViewGroup, Boolean) -> ViewBinding>()

    /**
     * 注册 Item 类型和对应的 ViewBinding
     * 
     * @param type 唯一的类型标识 (从 T.itemType 返回的值)
     * @param inflate ViewBinding 的静态 inflate 方法，例如 ItemTextBinding::inflate
     */
    fun <VB : ViewBinding> addItemBinding(
        type: Int,
        inflate: (LayoutInflater, ViewGroup, Boolean) -> VB
    ) {
        // addItemType(type, 0) 是为了占位，因为 BRVAH 内部维护了一个 map
        // 我们传入 layoutResId=0，然后重写 onCreateDefViewHolder 来用 ViewBinding 接管
        addItemType(type, 0)
        bindingInflateMap[type] = inflate
    }

    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflateFunc = bindingInflateMap[viewType]
        if (inflateFunc != null) {
            val binding = inflateFunc.invoke(LayoutInflater.from(parent.context), parent, false)
            return BaseMultiBindingViewHolder(binding)
        }
        // Fallback，如果未注册 Binding，尝试走原生 BRVAH 逻辑
        return super.onCreateDefViewHolder(parent, viewType)
    }

    override fun convert(holder: BaseViewHolder, item: T) {
        if (holder is BaseMultiBindingViewHolder<*>) {
            // 将具体的 Binding、数据和位置交由业务侧进行分发处理
            convert(holder.binding, item, holder.itemViewType, holder.layoutPosition)
        }
    }

    /**
     * 业务侧需要重写此方法，根据不同的 itemType，将 binding 强转为对应的 ViewBinding 类进行 UI 赋值
     * 
     * @param binding 父类 ViewBinding，你需要使用 as 强转为具体的 Binding (如 binding as ItemTextBinding)
     * @param item 数据模型
     * @param itemType 当前的 Item 类型
     * @param position 列表中的位置
     */
    abstract fun convert(binding: ViewBinding, item: T, itemType: Int, position: Int)
}

/**
 * 支持多类型的 ViewBinding 容器
 */
class BaseMultiBindingViewHolder<VB : ViewBinding>(val binding: VB) : BaseViewHolder(binding.root)
