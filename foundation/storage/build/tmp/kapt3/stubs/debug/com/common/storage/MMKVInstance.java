package com.common.storage;

/**
 * MMKV 实例封装类
 * 允许传入自定义 mmapID 以实现多实例/分场景存储
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0005\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0005\u001a\u00020\u0006J\u0018\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\bJ\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\rJ\u0018\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u000fJ\u0018\u0010\u0010\u001a\u00020\u00112\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u0011J\u0018\u0010\u0012\u001a\u00020\u00132\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u0013J\u0018\u0010\u0014\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\nJ\u0018\u0010\u0015\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\n2\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001J\u000e\u0010\u0017\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/common/storage/MMKVInstance;", "", "mmkv", "Lcom/tencent/mmkv/MMKV;", "(Lcom/tencent/mmkv/MMKV;)V", "clear", "", "getBoolean", "", "key", "", "defaultValue", "getDouble", "", "getFloat", "", "getInt", "", "getLong", "", "getString", "put", "value", "remove", "storage_debug"})
public final class MMKVInstance {
    @org.jetbrains.annotations.NotNull
    private final com.tencent.mmkv.MMKV mmkv = null;
    
    public MMKVInstance(@org.jetbrains.annotations.NotNull
    com.tencent.mmkv.MMKV mmkv) {
        super();
    }
    
    /**
     * 保存数据
     */
    public final void put(@org.jetbrains.annotations.NotNull
    java.lang.String key, @org.jetbrains.annotations.Nullable
    java.lang.Object value) {
    }
    
    /**
     * 获取 String
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getString(@org.jetbrains.annotations.NotNull
    java.lang.String key, @org.jetbrains.annotations.NotNull
    java.lang.String defaultValue) {
        return null;
    }
    
    /**
     * 获取 Int
     */
    public final int getInt(@org.jetbrains.annotations.NotNull
    java.lang.String key, int defaultValue) {
        return 0;
    }
    
    /**
     * 获取 Boolean
     */
    public final boolean getBoolean(@org.jetbrains.annotations.NotNull
    java.lang.String key, boolean defaultValue) {
        return false;
    }
    
    /**
     * 获取 Float
     */
    public final float getFloat(@org.jetbrains.annotations.NotNull
    java.lang.String key, float defaultValue) {
        return 0.0F;
    }
    
    /**
     * 获取 Long
     */
    public final long getLong(@org.jetbrains.annotations.NotNull
    java.lang.String key, long defaultValue) {
        return 0L;
    }
    
    /**
     * 获取 Double
     */
    public final double getDouble(@org.jetbrains.annotations.NotNull
    java.lang.String key, double defaultValue) {
        return 0.0;
    }
    
    /**
     * 移除某个 Key
     */
    public final void remove(@org.jetbrains.annotations.NotNull
    java.lang.String key) {
    }
    
    /**
     * 清除当前实例下的所有数据
     */
    public final void clear() {
    }
}