package com.common.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.widget.TextView
import com.common.R

/**
 * 通用加载弹窗
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.Theme_Dialog_Transparent) {

    private var tvMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)
        
        tvMessage = findViewById(R.id.tv_message)
        
        setCanceledOnTouchOutside(false)
    }

    fun setMessage(message: String) {
        tvMessage?.text = message
    }
}