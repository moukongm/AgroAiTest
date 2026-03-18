package com.common.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.common.R

/**
 * 通用顶部导航栏
 */
class TopBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var ivBack: ImageView
    private var tvTitle: TextView
    private var tvRight: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_top_bar, this, true)
        ivBack = findViewById(R.id.iv_back)
        tvTitle = findViewById(R.id.tv_title)
        tvRight = findViewById(R.id.tv_right)

        // 默认点击返回销毁Activity
        ivBack.setOnClickListener {
            if (context is android.app.Activity) {
                context.finish()
            }
        }
    }

    fun setTitle(title: String) {
        tvTitle.text = title
    }

    fun setRightText(text: String, listener: OnClickListener? = null) {
        tvRight.text = text
        tvRight.visibility = VISIBLE
        tvRight.setOnClickListener(listener)
    }

    fun setBackListener(listener: OnClickListener) {
        ivBack.setOnClickListener(listener)
    }
}