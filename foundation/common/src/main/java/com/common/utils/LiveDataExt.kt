package com.common.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * LiveData 相关的 Kotlin 高级扩展与组合工具类
 * 旨在简化日常开发中 LiveData 的操作、订阅以及多源组合逻辑。
 */

/**
 * 安全地观察 LiveData，仅在数据非 null 时触发回调，避免判空样板代码
 */
inline fun <T> LiveData<T>.observeNonNull(
    owner: LifecycleOwner,
    crossinline observer: (T) -> Unit
) {
    this.observe(owner, Observer { value ->
        if (value != null) {
            observer(value)
        }
    })
}

/**
 * 包装 MediatorLiveData，将两个 LiveData 的值进行合并计算，输出一个新的 LiveData
 * 任意一个源数据发生变化都会触发合并操作。
 * @param source1 第一个数据源
 * @param source2 第二个数据源
 * @param combiner 合并计算的逻辑块
 */
fun <A, B, R> combineLatest(
    source1: LiveData<A>,
    source2: LiveData<B>,
    combiner: (A?, B?) -> R
): LiveData<R> {
    val mediator = MediatorLiveData<R>()
    
    val combineFunc = {
        mediator.value = combiner(source1.value, source2.value)
    }

    mediator.addSource(source1) { combineFunc() }
    mediator.addSource(source2) { combineFunc() }
    
    return mediator
}

/**
 * 包装 MediatorLiveData，合并三个 LiveData。
 */
fun <A, B, C, R> combineLatest(
    source1: LiveData<A>,
    source2: LiveData<B>,
    source3: LiveData<C>,
    combiner: (A?, B?, C?) -> R
): LiveData<R> {
    val mediator = MediatorLiveData<R>()
    
    val combineFunc = {
        mediator.value = combiner(source1.value, source2.value, source3.value)
    }

    mediator.addSource(source1) { combineFunc() }
    mediator.addSource(source2) { combineFunc() }
    mediator.addSource(source3) { combineFunc() }
    
    return mediator
}

/**
 * SingleLiveEvent 实现思想的简易版，只响应订阅后的数据变化（防倒灌）
 * 适用于 Toast 提示、页面跳转等一次性事件。
 */
class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = java.util.concurrent.atomic.AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            LogUtils.w("SingleLiveEvent", "Multiple observers registered but only one will be notified of changes.")
        }

        // 包装原有的 Observer，加入 pending 标志位控制
        super.observe(owner, Observer { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        })
    }

    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    /**
     * 无参调用，方便作为纯事件通知使用（如 SingleLiveEvent<Unit>）
     */
    fun call() {
        value = null
    }
}
