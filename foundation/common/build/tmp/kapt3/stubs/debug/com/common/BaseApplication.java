package com.common;

/**
 * 基础 Application
 * 负责全局初始化，如 ARouter, MMKV 等
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\b\u0016\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H\u0002J\b\u0010\u0005\u001a\u00020\u0004H\u0002J\b\u0010\u0006\u001a\u00020\u0004H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/common/BaseApplication;", "Landroid/app/Application;", "()V", "initARouter", "", "initMMKV", "onCreate", "common_debug"})
public class BaseApplication extends android.app.Application {
    
    public BaseApplication() {
        super();
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    /**
     * 初始化 ARouter 路由框架
     */
    private final void initARouter() {
    }
    
    /**
     * 初始化 MMKV 存储
     */
    private final void initMMKV() {
    }
}