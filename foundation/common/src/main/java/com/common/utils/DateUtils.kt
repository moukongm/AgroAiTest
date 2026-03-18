package com.common.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * 日期时间工具类
 */
object DateUtils {

    const val FORMAT_YMD_HMS = "yyyy-MM-dd HH:mm:ss"
    const val FORMAT_YMD = "yyyy-MM-dd"

    /**
     * 获取当前时间字符串
     */
    fun getCurrentTime(format: String = FORMAT_YMD_HMS): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * 时间戳转字符串
     */
    fun formatTime(timestamp: Long, format: String = FORMAT_YMD_HMS): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
    
    /**
     * 格式化友好时间 (如：刚刚，x分钟前)
     */
    fun getFriendlyTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = (now - timestamp) / 1000

        return when {
            diff < 60 -> "刚刚"
            diff < 3600 -> "${diff / 60}分钟前"
            diff < 86400 -> "${diff / 3600}小时前"
            diff < 2592000 -> "${diff / 86400}天前"
            else -> formatTime(timestamp, FORMAT_YMD)
        }
    }
}