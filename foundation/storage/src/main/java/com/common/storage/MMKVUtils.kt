package com.common.storage

import android.content.Context
import com.tencent.mmkv.MMKV

/**
 * MMKV 工具类
 * 用于替代 SharedPreferences 进行高性能的键值对存储
 */
object MMKVUtils {

    /**
     * 初始化 MMKV，已经在 Application 中调用过
     */
    fun init(context: Context) {
        MMKV.initialize(context)
    }

    /**
     * 获取全局默认的 MMKV 实例
     */
    val default: MMKVInstance by lazy {
        MMKVInstance(MMKV.defaultMMKV())
    }

    /**
     * 根据场景/业务名称获取自定义的 MMKV 实例
     * 例如：用户模块、设置模块等分开存储，清除时互不影响
     * 
     * @param mmapID 实例的唯一标识（相当于 SharedPreferences 的文件名）
     */
    fun custom(mmapID: String): MMKVInstance {
        return MMKVInstance(MMKV.mmkvWithID(mmapID)!!)
    }

    fun put(key: String, value: Any?) = default.put(key, value)
    fun getString(key: String, defaultValue: String = "") = default.getString(key, defaultValue)
    fun getInt(key: String, defaultValue: Int = 0) = default.getInt(key, defaultValue)
    fun getBoolean(key: String, defaultValue: Boolean = false) = default.getBoolean(key, defaultValue)
    fun getFloat(key: String, defaultValue: Float = 0f) = default.getFloat(key, defaultValue)
    fun getLong(key: String, defaultValue: Long = 0L) = default.getLong(key, defaultValue)
    fun getDouble(key: String, defaultValue: Double = 0.0) = default.getDouble(key, defaultValue)
    fun remove(key: String) = default.remove(key)
    fun clear() = default.clear()
}

/**
 * MMKV 实例封装类
 * 允许传入自定义 mmapID 以实现多实例/分场景存储
 */
class MMKVInstance(private val mmkv: MMKV) {

    /**
     * 保存数据
     */
    fun put(key: String, value: Any?) {
        when (value) {
            is String -> mmkv.encode(key, value)
            is Int -> mmkv.encode(key, value)
            is Boolean -> mmkv.encode(key, value)
            is Float -> mmkv.encode(key, value)
            is Long -> mmkv.encode(key, value)
            is Double -> mmkv.encode(key, value)
            is ByteArray -> mmkv.encode(key, value)
            else -> if (value != null) mmkv.encode(key, value.toString())
        }
    }

    /**
     * 获取 String
     */
    fun getString(key: String, defaultValue: String = ""): String {
        return mmkv.decodeString(key, defaultValue) ?: defaultValue
    }

    /**
     * 获取 Int
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return mmkv.decodeInt(key, defaultValue)
    }

    /**
     * 获取 Boolean
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return mmkv.decodeBool(key, defaultValue)
    }

    /**
     * 获取 Float
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return mmkv.decodeFloat(key, defaultValue)
    }

    /**
     * 获取 Long
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return mmkv.decodeLong(key, defaultValue)
    }

    /**
     * 获取 Double
     */
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return mmkv.decodeDouble(key, defaultValue)
    }

    /**
     * 移除某个 Key
     */
    fun remove(key: String) {
        mmkv.removeValueForKey(key)
    }

    /**
     * 清除当前实例下的所有数据
     */
    fun clear() {
        mmkv.clearAll()
    }
}
