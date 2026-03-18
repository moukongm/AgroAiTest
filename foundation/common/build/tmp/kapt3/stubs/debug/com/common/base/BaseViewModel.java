package com.common.base;

/**
 * ViewModel 基类
 * 负责管理 RxJava 的订阅生命周期，防止内存泄漏
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0016\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0004J\b\u0010\t\u001a\u00020\u0006H\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/common/base/BaseViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "compositeDisposable", "Lio/reactivex/rxjava3/disposables/CompositeDisposable;", "addDisposable", "", "disposable", "Lio/reactivex/rxjava3/disposables/Disposable;", "onCleared", "common_debug"})
public class BaseViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final io.reactivex.rxjava3.disposables.CompositeDisposable compositeDisposable = null;
    
    public BaseViewModel() {
        super();
    }
    
    /**
     * 将 Disposable 添加到管理列表
     * 当 ViewModel 销毁时，会自动取消订阅
     */
    protected final void addDisposable(@org.jetbrains.annotations.NotNull
    io.reactivex.rxjava3.disposables.Disposable disposable) {
    }
    
    /**
     * ViewModel 清理回调
     * 在此取消所有未完成的 RxJava 订阅
     */
    @java.lang.Override
    protected void onCleared() {
    }
}