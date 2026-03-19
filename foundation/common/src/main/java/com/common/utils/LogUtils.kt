package com.common.utils

import android.util.Log

/**
 * 全局统一日志管理工具
 *
 * 特性：
 * 1. 统一开关，Release 环境自动关闭日志输出（或仅输出严重错误）
 * 2. 自动获取调用类的类名作为 TAG，减少硬编码
 * 3. 规范化日志格式，支持超长文本打印（突破 Logcat 4000 字符限制）
 * 4. 支持格式化输出 JSON/XML 等复杂数据结构（可扩展）
 */
object LogUtils {
    // 日志全局开关，可通过 BuildConfig.DEBUG 等初始化设置
    var isDebug = true

    // 默认 TAG
    private const val DEFAULT_TAG = "AgroAi"

    // Logcat 每条日志最大长度
    private const val MAX_LOG_LENGTH = 4000

    /**
     * 自动生成 TAG
     * 提取调用此工具类的真实类名，并附加上方法名和行号信息
     */
    private fun generateTag(): String {
        val stackTrace = Thread.currentThread().stackTrace
        // 索引 5 通常是调用 LogUtils 业务类的位置 (因为调用层级：业务类 -> LogUtils.v() -> printLog() -> generateTag())
        if (stackTrace.size >= 6) {
            val element = stackTrace[5]
            var className = element.className
            
            val moduleName = extractModuleName(className)
            
            className = className.substring(className.lastIndexOf('.') + 1)
            // 处理内部类
            val index = className.indexOf('$')
            if (index > 0) {
                className = className.substring(0, index)
            }
            return "$moduleName-$className"
        }
        return DEFAULT_TAG
    }

    /**
     * 从类名全路径中提取模块名作为前缀
     * 例如 com.common.utils.LogUtils -> common
     * com.user.LoginActivity -> user
     * com.main.HomeActivity -> main
     */
    private fun extractModuleName(fullClassName: String): String {
        try {
            val parts = fullClassName.split(".")
            // 假设包名结构为 com.xxx.yyy，取第二段作为模块名
            if (parts.size >= 2) {
                // 如果是 com.agroai 这种直接在 app 模块下的，可以特殊处理
                if (parts[0] == "com" && parts[1] == "agroai") {
                    return "app"
                }
                if (parts[0] == "com") {
                    return parts[1]
                }
            }
        } catch (e: Exception) {
            // 解析失败时降级
        }
        return DEFAULT_TAG
    }

    /**
     * 内部基础打印逻辑，处理超长字符串
     */
    private fun printLog(type: Int, tag: String, msg: String, tr: Throwable? = null) {
        if (!isDebug && type < Log.WARN) return

        val finalTag = tag.ifEmpty { generateTag() }
        val finalMsg = msg.ifEmpty { "Log message is empty" }

        // 处理超长字符串
        val length = finalMsg.length
        if (length <= MAX_LOG_LENGTH) {
            log(type, finalTag, finalMsg, tr)
        } else {
            var i = 0
            while (i < length) {
                val end = if (i + MAX_LOG_LENGTH < length) i + MAX_LOG_LENGTH else length
                val subMsg = finalMsg.substring(i, end)
                log(type, finalTag, subMsg, if (i == 0) tr else null)
                i += MAX_LOG_LENGTH
            }
        }
    }

    private fun log(type: Int, tag: String, msg: String, tr: Throwable?) {
        when (type) {
            Log.VERBOSE -> if (tr == null) Log.v(tag, msg) else Log.v(tag, msg, tr)
            Log.DEBUG -> if (tr == null) Log.d(tag, msg) else Log.d(tag, msg, tr)
            Log.INFO -> if (tr == null) Log.i(tag, msg) else Log.i(tag, msg, tr)
            Log.WARN -> if (tr == null) Log.w(tag, msg) else Log.w(tag, msg, tr)
            Log.ERROR -> if (tr == null) Log.e(tag, msg) else Log.e(tag, msg, tr)
        }
    }

    // =====================================================================
    // 常用打印 API (自动生成 TAG)
    // =====================================================================

    fun v(msg: String) = printLog(Log.VERBOSE, "", msg)
    fun d(msg: String) = printLog(Log.DEBUG, "", msg)
    fun i(msg: String) = printLog(Log.INFO, "", msg)
    fun w(msg: String, tr: Throwable? = null) = printLog(Log.WARN, "", msg, tr)
    fun e(msg: String, tr: Throwable? = null) = printLog(Log.ERROR, "", msg, tr)

    // =====================================================================
    // 指定 TAG 打印 API
    // =====================================================================

    fun v(tag: String, msg: String) = printLog(Log.VERBOSE, tag, msg)
    fun d(tag: String, msg: String) = printLog(Log.DEBUG, tag, msg)
    fun i(tag: String, msg: String) = printLog(Log.INFO, tag, msg)
    fun w(tag: String, msg: String, tr: Throwable? = null) = printLog(Log.WARN, tag, msg, tr)
    fun e(tag: String, msg: String, tr: Throwable? = null) = printLog(Log.ERROR, tag, msg, tr)
    
}
