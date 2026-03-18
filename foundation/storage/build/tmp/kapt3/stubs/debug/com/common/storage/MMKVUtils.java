package com.common.storage;

/**
 * MMKV 工具类
 * 用于替代 SharedPreferences 进行高性能的键值对存储
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\t\u001a\u00020\nJ\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\f\u001a\u00020\rJ\u0018\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\u000fJ\u0018\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\u0013J\u0018\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\u0015J\u0018\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\u0017J\u0018\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\u0019J\u0018\u0010\u001a\u001a\u00020\r2\u0006\u0010\u0010\u001a\u00020\r2\b\b\u0002\u0010\u0011\u001a\u00020\rJ\u000e\u0010\u001b\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\u001dJ\u0018\u0010\u001e\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\r2\b\u0010\u001f\u001a\u0004\u0018\u00010\u0001J\u000e\u0010 \u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\rR\u001b\u0010\u0003\u001a\u00020\u00048FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006!"}, d2 = {"Lcom/common/storage/MMKVUtils;", "", "()V", "default", "Lcom/common/storage/MMKVInstance;", "getDefault", "()Lcom/common/storage/MMKVInstance;", "default$delegate", "Lkotlin/Lazy;", "clear", "", "custom", "mmapID", "", "getBoolean", "", "key", "defaultValue", "getDouble", "", "getFloat", "", "getInt", "", "getLong", "", "getString", "init", "context", "Landroid/content/Context;", "put", "value", "remove", "storage_debug"})
public final class MMKVUtils {
    
    /**
     * 获取全局默认的 MMKV 实例
     */
    @org.jetbrains.annotations.NotNull
    private static final kotlin.Lazy default$delegate = null;
    @org.jetbrains.annotations.NotNull
    public static final com.common.storage.MMKVUtils INSTANCE = null;
    
    private MMKVUtils() {
        super();
    }
    
    /**
     * 初始化 MMKV，已经在 Application 中调用过
     */
    public final void init(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
    }
    
    /**
     * 获取全局默认的 MMKV 实例
     */
    @org.jetbrains.annotations.NotNull
    public final com.common.storage.MMKVInstance getDefault() {
        return null;
    }
    
    /**
     * 根据场景/业务名称获取自定义的 MMKV 实例
     * 例如：用户模块、设置模块等分开存储，清除时互不影响
     *
     * @param mmapID 实例的唯一标识（相当于 SharedPreferences 的文件名）
     */
    @org.jetbrains.annotations.NotNull
    public final com.common.storage.MMKVInstance custom(@org.jetbrains.annotations.NotNull
    java.lang.String mmapID) {
        return null;
    }
    
    public final void put(@org.jetbrains.annotations.NotNull
    java.lang.String key, @org.jetbrains.annotations.Nullable
    java.lang.Object value) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getString(@org.jetbrains.annotations.NotNull
    java.lang.String key, @org.jetbrains.annotations.NotNull
    java.lang.String defaultValue) {
        return null;
    }
    
    public final int getInt(@org.jetbrains.annotations.NotNull
    java.lang.String key, int defaultValue) {
        return 0;
    }
    
    public final boolean getBoolean(@org.jetbrains.annotations.NotNull
    java.lang.String key, boolean defaultValue) {
        return false;
    }
    
    public final float getFloat(@org.jetbrains.annotations.NotNull
    java.lang.String key, float defaultValue) {
        return 0.0F;
    }
    
    public final long getLong(@org.jetbrains.annotations.NotNull
    java.lang.String key, long defaultValue) {
        return 0L;
    }
    
    public final double getDouble(@org.jetbrains.annotations.NotNull
    java.lang.String key, double defaultValue) {
        return 0.0;
    }
    
    public final void remove(@org.jetbrains.annotations.NotNull
    java.lang.String key) {
    }
    
    public final void clear() {
    }
}