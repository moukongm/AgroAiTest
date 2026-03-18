package com.common.utils;

/**
 * 图片加载工具类
 * 基于 Coil 封装，提供常用的图片加载方法
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bJ\u0018\u0010\t\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\bJ\u0016\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\fJ \u0010\r\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\u0006\u0010\u000e\u001a\u00020\u000f\u00a8\u0006\u0010"}, d2 = {"Lcom/common/utils/ImageLoader;", "", "()V", "load", "", "imageView", "Landroid/widget/ImageView;", "url", "", "loadCircle", "loadLocal", "resId", "", "loadRounded", "radius", "", "common_debug"})
public final class ImageLoader {
    @org.jetbrains.annotations.NotNull
    public static final com.common.utils.ImageLoader INSTANCE = null;
    
    private ImageLoader() {
        super();
    }
    
    /**
     * 加载普通图片
     *
     * @param imageView 目标 ImageView
     * @param url 图片链接
     */
    public final void load(@org.jetbrains.annotations.NotNull
    android.widget.ImageView imageView, @org.jetbrains.annotations.Nullable
    java.lang.String url) {
    }
    
    /**
     * 加载圆形图片
     *
     * @param imageView 目标 ImageView
     * @param url 图片链接
     */
    public final void loadCircle(@org.jetbrains.annotations.NotNull
    android.widget.ImageView imageView, @org.jetbrains.annotations.Nullable
    java.lang.String url) {
    }
    
    /**
     * 加载圆角图片
     *
     * @param imageView 目标 ImageView
     * @param url 图片链接
     * @param radius 圆角半径 (px)
     */
    public final void loadRounded(@org.jetbrains.annotations.NotNull
    android.widget.ImageView imageView, @org.jetbrains.annotations.Nullable
    java.lang.String url, float radius) {
    }
    
    /**
     * 加载本地资源图片
     *
     * @param imageView 目标 ImageView
     * @param resId 本地资源 ID (R.drawable.xxx)
     */
    public final void loadLocal(@org.jetbrains.annotations.NotNull
    android.widget.ImageView imageView, int resId) {
    }
}