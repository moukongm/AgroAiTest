package com.uikit.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import java.lang.reflect.ParameterizedType

/**
 * 基于 ViewBinding 的通用 RecyclerView Adapter 基类
 * 
 * 继承自强大的 BRVAH (BaseRecyclerViewAdapterHelper)。
 * 通过泛型和反射自动初始化 ViewBinding，极大减少业务层写 Adapter 时的样板代码。
 *
 * @param T 实体数据类型
 * @param VB 对应的 ViewBinding 类型
 */
abstract class BaseBindingAdapter<T, VB : ViewBinding>(
    private val customLayoutResId: Int = 0,
    data: MutableList<T>? = null
) : BaseQuickAdapter<T, BaseBindingViewHolder<VB>>(customLayoutResId, data) {

    /**
     * 重写此方法，利用反射根据泛型 VB 自动加载布局并绑定
     */
    @Suppress("UNCHECKED_CAST")
    override fun onCreateDefViewHolder(parent: ViewGroup, viewType: Int): BaseBindingViewHolder<VB> {
        // 如果传入了自定义的 layoutResId 走默认逻辑，但通常我们希望使用 ViewBinding
        if (customLayoutResId != 0) {
            return super.onCreateDefViewHolder(parent, viewType)
        }

        val type = javaClass.genericSuperclass as ParameterizedType
        val clazz = type.actualTypeArguments[1] as Class<VB>
        val method = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        val binding = method.invoke(null, LayoutInflater.from(parent.context), parent, false) as VB
        return BaseBindingViewHolder(binding)
    }

    /**
     * 业务层只需重写此方法进行数据绑定
     * 
     * @param binding 自动生成的 ViewBinding 对象，可以直接 .tvName 等操作 View
     * @param item 当前项的数据模型
     * @param position 当前项的位置
     */
    abstract fun convert(binding: VB, item: T, position: Int)

    override fun convert(holder: BaseBindingViewHolder<VB>, item: T) {
        // 将回调转发给带 Binding 的抽象方法
        convert(holder.binding, item, holder.layoutPosition)
    }
}

/**
 * 包装 ViewBinding 的专属 ViewHolder
 */
class BaseBindingViewHolder<VB : ViewBinding>(val binding: VB) : BaseViewHolder(binding.root)
