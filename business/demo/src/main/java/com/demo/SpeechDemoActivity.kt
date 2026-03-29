package com.demo

import android.annotation.SuppressLint
import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.bytedance.speech.speechengine.SpeechEngineDefines
import com.common.base.BaseActivity
import com.common.router.RouterPath
import com.common.speech.SpeechManager
import com.common.utils.LogUtils
import com.common.utils.PermissionUtils
import com.common.utils.ThreadUtils
import com.common.utils.ToastUtils
import com.common.utils.setOnDebouncedClickListener
import com.demo.databinding.ActivitySpeechDemoBinding
import org.json.JSONObject

@Route(path = RouterPath.SPEECH_DEMO_ACTIVITY)
class SpeechDemoActivity : BaseActivity<ActivitySpeechDemoBinding>() {

    override fun getViewBinding(): ActivitySpeechDemoBinding {
        return ActivitySpeechDemoBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.topBar.setTitle("豆包语音 Demo")
        // topBar 默认自带 finish 行为

        binding.btnStartRecord.setOnDebouncedClickListener {
            PermissionUtils.request(
                this,
                listOf(
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                onGranted = {
                    startRecording()
                },
                onDenied = {
                    ToastUtils.showShort(this, "需要录音和存储权限才能使用语音功能")
                }
            )
        }

        binding.btnStopRecord.setOnDebouncedClickListener {
            stopRecording()
        }
        
        binding.btnCancelRecord.setOnDebouncedClickListener {
            cancelRecording()
        }

        initSpeechEngineCallback()
    }

    override fun initData() {
    }

    private fun initSpeechEngineCallback() {
        val engine = SpeechManager.getEngine()
        if (engine == null) {
            appendLog("错误：语音引擎未初始化！")
            return
        }

        engine.setListener { type, data, length ->
            ThreadUtils.runOnUiThread {
                when (type) {
                    SpeechEngineDefines.MESSAGE_TYPE_DIALOG_CONNECTION_STARTED -> {
                        appendLog("网络连接已建立")
                    }
                    SpeechEngineDefines.MESSAGE_TYPE_DIALOG_SESSION_STARTED -> {
                        appendLog("会话已启动，请说话...")
                    }
                    SpeechEngineDefines.MESSAGE_TYPE_ENGINE_START -> {
                        appendLog("开始工作 (Start)")
                    }
                    SpeechEngineDefines.MESSAGE_TYPE_ENGINE_STOP -> {
                        appendLog("停止工作 (Stop)")
                    }
                    SpeechEngineDefines.MESSAGE_TYPE_ENGINE_ERROR -> {
                        appendLog("引擎错误: ${String(data)}")
                    }
                    SpeechEngineDefines.MESSAGE_TYPE_DIALOG_CHAT_RESPONSE -> {
                        try {
                            val resultStr = String(data)
                            val jsonObj = JSONObject(resultStr)
                            val text = jsonObj.optString("text", "")
                            val reqId = jsonObj.optString("reqid", "")
                            appendLog("对话结果: reqId=$reqId, text=$text")
                        } catch (e: Exception) {
                            appendLog("解析结果异常: ${e.message}")
                        }
                    }
                    SpeechEngineDefines.MESSAGE_TYPE_DIALOG_ASR_RESPONSE -> {
                        try {
                            val resultStr = String(data)
                            appendLog("识别状态: $resultStr")
                        } catch (e: Exception) {
                            // ignore
                        }
                    }
                    else -> {
                        // 忽略其他高频消息
                        // LogUtils.d("SpeechEngine", "收到消息 type: $type")
                    }
                }
            }
        }
    }

    private fun startRecording() {
        val engine = SpeechManager.getEngine()
        if (engine == null) {
            ToastUtils.showShort(this, "语音引擎未初始化")
            return
        }

        appendLog("----- 开始录音 -----")
        // 启动连接与会话
        val ret = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_START_ENGINE, "")
        engine.sendDirective(SpeechEngineDefines.DIRECTIVE_DIALOG_START_CONNECTION, "")
        engine.sendDirective(SpeechEngineDefines.DIRECTIVE_DIALOG_START_SESSION, "")
        appendLog("启动引擎结果: $ret")
    }

    private fun stopRecording() {
        val engine = SpeechManager.getEngine()
        if (engine == null) return
        appendLog("----- 停止录音 (结束说话) -----")
        val ret = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_FINISH_TALKING, "")
        engine.sendDirective(SpeechEngineDefines.DIRECTIVE_DIALOG_FINISH_SESSION, "")
        appendLog("结束说话结果: $ret")
    }
    
    private fun cancelRecording() {
        val engine = SpeechManager.getEngine()
        if (engine == null) return
        appendLog("----- 取消录音 -----")
        val ret = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "")
        engine.sendDirective(SpeechEngineDefines.DIRECTIVE_DIALOG_CANCEL_SESSION, "")
        engine.sendDirective(SpeechEngineDefines.DIRECTIVE_DIALOG_FINISH_CONNECTION, "")
        appendLog("取消录音结果: $ret")
    }

    @SuppressLint("SetTextI18n")
    private fun appendLog(msg: String) {
        val current = binding.tvResult.text.toString()
        binding.tvResult.text = "$current\n> $msg"
        
        // 自动滚动到底部
        binding.tvResult.post {
            val scrollView = binding.tvResult.parent as? android.widget.ScrollView
            scrollView?.fullScroll(android.view.View.FOCUS_DOWN)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // 离开页面时停止引擎
        SpeechManager.getEngine()?.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "")
    }
}