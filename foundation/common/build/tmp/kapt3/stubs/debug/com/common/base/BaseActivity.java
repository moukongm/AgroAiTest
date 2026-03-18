package com.common.base;

/**
 * Activity 基类
 * 封装了 ViewBinding 的初始化和其他通用逻辑
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b&\u0018\u0000*\b\b\u0000\u0010\u0001*\u00020\u00022\u00020\u0003B\u0005\u00a2\u0006\u0002\u0010\u0004J\r\u0010\r\u001a\u00028\u0000H&\u00a2\u0006\u0002\u0010\u0007J\u0006\u0010\u000e\u001a\u00020\u000fJ\b\u0010\u0010\u001a\u00020\u000fH&J\b\u0010\u0011\u001a\u00020\u000fH\u0002J\b\u0010\u0012\u001a\u00020\u000fH&J\u0012\u0010\u0013\u001a\u00020\u000f2\b\u0010\u0014\u001a\u0004\u0018\u00010\u0015H\u0014J\b\u0010\u0016\u001a\u00020\u000fH\u0014J\u0010\u0010\u0017\u001a\u00020\u000f2\b\b\u0002\u0010\u0018\u001a\u00020\u0019R\u001c\u0010\u0005\u001a\u00028\u0000X\u0084.\u00a2\u0006\u0010\n\u0002\u0010\n\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u0010\u0010\u000b\u001a\u0004\u0018\u00010\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/common/base/BaseActivity;", "VB", "Landroidx/viewbinding/ViewBinding;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "getBinding", "()Landroidx/viewbinding/ViewBinding;", "setBinding", "(Landroidx/viewbinding/ViewBinding;)V", "Landroidx/viewbinding/ViewBinding;", "loadingDialog", "Lcom/common/widget/LoadingDialog;", "getViewBinding", "hideLoading", "", "initData", "initImmersiveStatusBar", "initView", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "showLoading", "message", "", "common_debug"})
public abstract class BaseActivity<VB extends androidx.viewbinding.ViewBinding> extends androidx.appcompat.app.AppCompatActivity {
    protected VB binding;
    @org.jetbrains.annotations.Nullable
    private com.common.widget.LoadingDialog loadingDialog;
    
    public BaseActivity() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    protected final VB getBinding() {
        return null;
    }
    
    protected final void setBinding(@org.jetbrains.annotations.NotNull
    VB p0) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    /**
     * 初始化沉浸式状态栏
     */
    private final void initImmersiveStatusBar() {
    }
    
    /**
     * 显示加载弹窗
     */
    public final void showLoading(@org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    /**
     * 隐藏加载弹窗
     */
    public final void hideLoading() {
    }
    
    @java.lang.Override
    protected void onDestroy() {
    }
    
    /**
     * 获取 ViewBinding 实例
     */
    @org.jetbrains.annotations.NotNull
    public abstract VB getViewBinding();
    
    /**
     * 初始化视图
     */
    public abstract void initView();
    
    /**
     * 初始化数据
     */
    public abstract void initData();
}