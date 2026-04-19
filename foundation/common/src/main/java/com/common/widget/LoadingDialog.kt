package com.common.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.common.R

/**
 * 通用加载弹窗
 */
class LoadingDialog(context: Context) : Dialog(context, R.style.Theme_Dialog_Transparent) {

    private var ivIcon: ImageView? = null

    private var tvMessage: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
        setContentView(R.layout.dialog_loading)
        tvMessage = findViewById(R.id.tv_message)


        ivIcon =  findViewById(R.id.ic_loading_dialog)
        
        setCanceledOnTouchOutside(false)
    }

    fun setMessage(message: String) {
        tvMessage?.text = message
    }
    override fun show() {
        super.show()
        val animation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation)
        ivIcon?.startAnimation(animation)
    }

    override fun dismiss() {
        ivIcon?.clearAnimation()
        super.dismiss()
    }

}