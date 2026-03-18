package com.network.model

import com.google.gson.annotations.SerializedName

/**
 * 通用网络响应基类
 * 所有接口返回的 JSON 最外层结构通常包含 code, message, data
 *
 * @param T 具体的数据类型
 */
data class BaseResponse<T>(
    @SerializedName("code") val code: Int,
    @SerializedName("msg") val message: String,
    @SerializedName("data") val data: T?
) {
    /**
     * 判断请求是否成功
     * 假设 code == 200 为成功，具体根据后端约定修改
     */
    fun isSuccess(): Boolean {
        return code == 200
    }
}
