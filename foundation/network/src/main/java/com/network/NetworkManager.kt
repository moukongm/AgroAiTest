package com.network

import android.annotation.SuppressLint
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * 网络请求管理类
 * 封装 Retrofit + OkHttp 配置，提供 Service 创建方法
 */
object NetworkManager {
    // 基础 URL，需要替换为实际项目地址或通过 BuildConfig 配置
    private const val BASE_URL = "https://api.example.com/"

    /**
     * 创建一个信任所有证书的 TrustManager（仅用于测试和解决部分模拟器证书校验失败的问题）
     */
    @SuppressLint("CustomX509TrustManager")
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        @SuppressLint("TrustAllX509TrustManager")
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    /**
     * OkHttpClient 实例
     * 配置了日志拦截器和超时时间，并忽略了 SSL 证书校验以解决 Chain validation failed 问题
     */
    val okHttpClient: OkHttpClient by lazy {
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        val sslSocketFactory = sslContext.socketFactory

        OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier { _, _ -> true }
            .addInterceptor(HttpLoggingInterceptor().apply {
                // 开发环境建议使用 BODY 级别，生产环境建议关闭或使用 BASIC
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(15, TimeUnit.SECONDS) // 连接超时
            .readTimeout(15, TimeUnit.SECONDS)    // 读取超时
            .writeTimeout(15, TimeUnit.SECONDS)   // 写入超时
            .build()
    }

    /**
     * Retrofit 实例
     * 配置了 Gson 解析器和 RxJava 适配器
     */
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()
    }

    /**
     * 全局唯一的 ApiService 实例，供各个模块直接调用
     */
    val api: ApiService by lazy {
        createService(ApiService::class.java)
    }

    /**
     * 创建 API Service 接口实例
     *
     * @param T Service 接口类型
     * @param serviceClass Service 接口 Class 对象
     */
    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}
