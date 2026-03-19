package com.common.utils

import android.os.SystemClock
import android.view.View

/**
 * View 相关的 Kotlin 扩展函数工具类
 * 提供防抖点击、可见性控制、以及其他常用 View 操作，简化开发。
 */

/**
 * 带有防抖功能的点击事件监听
 * @param debounceTime 防抖时间间隔，默认 500 毫秒
 * @param action 点击后的回调动作
 */
inline fun View.setOnDebouncedClickListener(
    debounceTime: Long = 500L,
    crossinline action: (View) -> Unit
) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            val currentTime = SystemClock.elapsedRealtime()
            if (currentTime - lastClickTime >= debounceTime) {
                lastClickTime = currentTime
                action(v)
            }
        }
    })
}

/**
 * 将 View 的可见性设置为 VISIBLE
 */
fun View.visible() {
    if (this.visibility != View.VISIBLE) {
        this.visibility = View.VISIBLE
    }
}

/**
 * 将 View 的可见性设置为 GONE
 */
fun View.gone() {
    if (this.visibility != View.GONE) {
        this.visibility = View.GONE
    }
}

/**
 * 将 View 的可见性设置为 INVISIBLE
 */
fun View.invisible() {
    if (this.visibility != View.INVISIBLE) {
        this.visibility = View.INVISIBLE
    }
}

/**
 * 根据布尔值动态控制 View 的可见性 (VISIBLE 或 GONE)
 * @param isVisible true -> VISIBLE, false -> GONE
 */
fun View.setVisible(isVisible: Boolean) {
    if (isVisible) {
        this.visible()
    } else {
        this.gone()
    }
}

/**
 * 切换可见性 (VISIBLE 与 GONE 之间切换)
 */
fun View.toggleVisibility() {
    if (this.visibility == View.VISIBLE) {
        this.gone()
    } else {
        this.visible()
    }
}

/**
 * 获取或设置 View 的 padding，简化四个方向的设置
 */
fun View.setPaddingAll(padding: Int) {
    this.setPadding(padding, padding, padding, padding)
}

/**
 * 延迟获取 View 宽高信息，常用于 onCreate 中获取尺寸
 */
inline fun View.afterMeasured(crossinline block: (View) -> Unit) {
    if (measuredWidth > 0 && measuredHeight > 0) {
        block(this)
    } else {
        viewTreeObserver.addOnGlobalLayoutListener(object : android.view.ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    block(this@afterMeasured)
                }
            }
        })
    }
}
