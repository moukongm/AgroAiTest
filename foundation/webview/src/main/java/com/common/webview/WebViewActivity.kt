package com.common.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.common.base.BaseActivity
import com.common.webview.databinding.ActivityWebViewBinding

/**
 * 通用的 WebView 页面
 */
class WebViewActivity : BaseActivity<ActivityWebViewBinding>() {

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TITLE = "extra_title"

        /**
         * 启动 WebViewActivity
         *
         * @param context Context
         * @param url 要加载的网页链接
         * @param title 页面标题（可选）
         */
        fun start(context: Context, url: String, title: String? = null) {
            val intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra(EXTRA_URL, url)
                putExtra(EXTRA_TITLE, title)
            }
            context.startActivity(intent)
        }
    }

    private var url: String? = null
    private var pageTitle: String? = null

    override fun getViewBinding(): ActivityWebViewBinding {
        return ActivityWebViewBinding.inflate(layoutInflater)
    }

    override fun initView() {
        url = intent.getStringExtra(EXTRA_URL)
        pageTitle = intent.getStringExtra(EXTRA_TITLE)

        binding.topBar.setTitle(pageTitle ?: "详情")
        
        WebViewUtils.initWebViewSettings(binding.webView)

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.VISIBLE
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.progress = newProgress
                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                if (pageTitle.isNullOrEmpty() && !title.isNullOrEmpty()) {
                    binding.topBar.setTitle(title)
                }
            }
        }
    }

    override fun initData() {
        url?.let {
            binding.webView.loadUrl(it)
        }
    }

    override fun onBackPressed() {
        if (binding.webView.canGoBack()) {
            binding.webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        binding.webView.destroy()
        super.onDestroy()
    }
}