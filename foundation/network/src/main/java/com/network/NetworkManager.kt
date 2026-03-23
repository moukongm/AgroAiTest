package com.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.openapitools.client.infrastructure.ApiClient
import com.agri.pest.client.api.ApiService
import java.util.concurrent.TimeUnit

/**
 * 网络请求管理类
 * 现在使用 AgriPest SDK 进行统一管理
 */
object NetworkManager {
    // 基础 URL
    private const val BASE_URL = "http://115.191.67.35:8080/"

    /**
     * 全局共享的 OkHttpClient
     * 这样可以复用连接池，并且方便 Coil 等其他库集成
     */
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    /**
     * SDK ApiClient 实例
     * 使用自定义的 okHttpClientBuilder 以便复用
     */
    private val apiClient by lazy {
        ApiClient(
            baseUrl = BASE_URL,
            okHttpClientBuilder = okHttpClient.newBuilder(),
            authNames = arrayOf("BearerAuth")
        )
    }

    /**
     * SDK 提供的全局 ApiService 实例
     */
    val api: ApiService by lazy {
        apiClient.createService(ApiService::class.java)
    }

    /**
     * 更新 Token (登录成功后调用)
     */
    fun setToken(token: String) {
        apiClient.setBearerToken(token)
    }
}
