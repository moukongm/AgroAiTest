package com.common.utils

import android.content.Context
import android.widget.Toast

/**
 * Toast 提示工具类
 * 防止 Toast 重复弹出，统一管理显示逻辑
 */
object ToastUtils {
    private var toast: Toast? = null

    /**
     * 显示短时 Toast
     *
     * @param context 上下文
     * @param message 提示内容
     */
    fun showShort(context: Context, message: String) {
        show(context, message, Toast.LENGTH_SHORT)
    }

    /**
     * 显示长时 Toast
     *
     * @param context 上下文
     * @param message 提示内容
     */
    fun showLong(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }

    /**
     * 内部显示实现
     * 如果当前有 Toast 正在显示，会先取消再显示新的
     */
    private fun show(context: Context, message: String, duration: Int) {
        toast?.cancel()
        toast = Toast.makeText(context.applicationContext, message, duration)
        toast?.show()
    }
}
