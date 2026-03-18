package com.common.utils;

/**
 * Toast 提示工具类
 * 防止 Toast 重复弹出，统一管理显示逻辑
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0016\u0010\r\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nJ\u0016\u0010\u000e\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\nR\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000f"}, d2 = {"Lcom/common/utils/ToastUtils;", "", "()V", "toast", "Landroid/widget/Toast;", "show", "", "context", "Landroid/content/Context;", "message", "", "duration", "", "showLong", "showShort", "common_debug"})
public final class ToastUtils {
    @org.jetbrains.annotations.Nullable
    private static android.widget.Toast toast;
    @org.jetbrains.annotations.NotNull
    public static final com.common.utils.ToastUtils INSTANCE = null;
    
    private ToastUtils() {
        super();
    }
    
    /**
     * 显示短时 Toast
     *
     * @param context 上下文
     * @param message 提示内容
     */
    public final void showShort(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    /**
     * 显示长时 Toast
     *
     * @param context 上下文
     * @param message 提示内容
     */
    public final void showLong(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    /**
     * 内部显示实现
     * 如果当前有 Toast 正在显示，会先取消再显示新的
     */
    private final void show(android.content.Context context, java.lang.String message, int duration) {
    }
}