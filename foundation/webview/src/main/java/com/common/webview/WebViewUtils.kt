package com.common.webview

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

/**
 * WebView 配置工具类
 */
object WebViewUtils {

    @SuppressLint("SetJavaScriptEnabled")
    fun initWebViewSettings(webView: WebView) {
        val settings = webView.settings
        
        // 开启 JavaScript
        settings.javaScriptEnabled = true
        
        // 开启 DOM 存储 API
        settings.domStorageEnabled = true
        
        // 支持视口元标签
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        
        // 允许混合内容 (HTTP和HTTPS)
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        
        // 缩放支持
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false

        // 默认的 WebViewClient 防止跳到外部浏览器
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    view?.loadUrl(url)
                    return true
                }
                return false
            }
        }
    }
}