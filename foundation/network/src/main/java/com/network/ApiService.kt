package com.network

import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET

/**
 * 全局网络请求接口定义
 * 后续所有的网络请求方法都可以添加在这里
 */
interface ApiService {
    /**
     * 测试接口: JSONPlaceholder (更稳定的测试接口)
     */
    @GET("https://jsonplaceholder.typicode.com/posts/1")
    fun getZen(): Observable<PostResponse>
}