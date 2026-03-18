package com.network

import com.google.gson.annotations.SerializedName

/**
 * 测试用的数据结构
 */
data class PostResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("body") val body: String
)