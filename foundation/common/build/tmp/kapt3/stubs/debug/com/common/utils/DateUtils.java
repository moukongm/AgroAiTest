package com.common.utils;

/**
 * 日期时间工具类
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0006\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\u0004J\u0010\u0010\n\u001a\u00020\u00042\b\b\u0002\u0010\t\u001a\u00020\u0004J\u000e\u0010\u000b\u001a\u00020\u00042\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/common/utils/DateUtils;", "", "()V", "FORMAT_YMD", "", "FORMAT_YMD_HMS", "formatTime", "timestamp", "", "format", "getCurrentTime", "getFriendlyTime", "common_debug"})
public final class DateUtils {
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String FORMAT_YMD_HMS = "yyyy-MM-dd HH:mm:ss";
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String FORMAT_YMD = "yyyy-MM-dd";
    @org.jetbrains.annotations.NotNull
    public static final com.common.utils.DateUtils INSTANCE = null;
    
    private DateUtils() {
        super();
    }
    
    /**
     * 获取当前时间字符串
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCurrentTime(@org.jetbrains.annotations.NotNull
    java.lang.String format) {
        return null;
    }
    
    /**
     * 时间戳转字符串
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String formatTime(long timestamp, @org.jetbrains.annotations.NotNull
    java.lang.String format) {
        return null;
    }
    
    /**
     * 格式化友好时间 (如：刚刚，x分钟前)
     */
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFriendlyTime(long timestamp) {
        return null;
    }
}