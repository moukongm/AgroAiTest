package com.common.base

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

/**
 * ViewModel 基类
 * 负责管理 RxJava 的订阅生命周期，防止内存泄漏
 */
open class BaseViewModel : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    /**
     * 将 Disposable 添加到管理列表
     * 当 ViewModel 销毁时，会自动取消订阅
     */
    protected fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    /**
     * ViewModel 清理回调
     * 在此取消所有未完成的 RxJava 订阅
     */
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
