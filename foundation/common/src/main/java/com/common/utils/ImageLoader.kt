package com.common.utils

import android.widget.ImageView
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation

/**
 * 图片加载工具类
 * 基于 Coil 封装，提供常用的图片加载方法
 */
object ImageLoader {

    /**
     * 加载普通图片
     *
     * @param imageView 目标 ImageView
     * @param url 图片链接
     */
    fun load(imageView: ImageView, url: String?) {
        imageView.load(url) {
            crossfade(true)
        }
    }

    /**
     * 加载普通图片（带占位图）
     *
     * @param imageView 目标 ImageView
     * @param url 图片链接
     * @param placeholderResId 占位图资源 ID
     * @param errorResId 错误图资源 ID
     */
    fun load(imageView: ImageView, url: String?, placeholderResId: Int, errorResId: Int) {
        imageView.load(url) {
            crossfade(true)
            placeholder(placeholderResId)
            error(errorResId)
        }
    }

    /**
     * 加载圆形图片
     *
     * @param imageView 目标 ImageView
     * @param url 图片链接
     */
    fun loadCircle(imageView: ImageView, url: String?) {
        imageView.load(url) {
            crossfade(true)
            transformations(CircleCropTransformation())
        }
    }

    /**
     * 加载圆角图片
     *
     * @param imageView 目标 ImageView
     * @param url 图片链接
     * @param radius 圆角半径 (px)
     */
    fun loadRounded(imageView: ImageView, url: String?, radius: Float) {
        imageView.load(url) {
            crossfade(true)
            transformations(RoundedCornersTransformation(radius))
        }
    }

    /**
     * 加载本地资源图片
     *
     * @param imageView 目标 ImageView
     * @param resId 本地资源 ID (R.drawable.xxx)
     */
    fun loadLocal(imageView: ImageView, resId: Int) {
        imageView.load(resId) {
            crossfade(true)
        }
    }
}
