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
                    android.Manifest.permission.READ_PHONE_STATE
                ),
                onGranted = {
                    startRecording()
                },
                onDenied = {
                    ToastUtils.showShort(this, "需要录音权限才能使用语音功能")
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
                            // AI 返回的对话文本和中间状态都是在 content 字段下
                            // LogUtils.d("SpeechEngine", "CHAT_RESPONSE: $resultStr")
                            val jsonObj = JSONObject(resultStr)
                            
                            // 豆包 API v3 的返回结构中，实际对话文本可能嵌套在 choices -> messages 或者其他字段中
                            // 我们可以直接打印整个回调看看数据结构，以便正确解析
                            appendLog("=> AI: $resultStr")
                        } catch (e: Exception) {
                            appendLog("解析结果异常: ${e.message}")
                        }
                    }
                    SpeechEngineDefines.MESSAGE_TYPE_DIALOG_ASR_RESPONSE -> {
                        try {
                            val resultStr = String(data)
                            val jsonObj = JSONObject(resultStr)
                            // ASR 结果也是，我们先打印出来看看
                            appendLog("=> ASR: $resultStr")
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
        // 同步停止上一次
        engine.sendDirective(SpeechEngineDefines.DIRECTIVE_SYNC_STOP_ENGINE, "")
        // 发送启动指令，并在这里配置机器人的身份/音色等信息
        // bot_name: 对应您在豆包火山引擎后台配置的机器人角色名称
        val startJson = """
            {
                "dialog": {
                    "bot_name": "小农"
                }
            }
        """.trimIndent()
        val ret = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_START_ENGINE, startJson)
        if (ret != SpeechEngineDefines.ERR_NO_ERROR) {
            appendLog("启动引擎失败: $ret")
            return
        }
        
        appendLog("启动引擎结果: $ret")
        
        // 增加开场白
        val helloJson = "{\"content\": \"我是小农，有什么病虫害相关的问题你都可以问我呀，我会帮你解答的。\"}"
        val helloRet = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_EVENT_SAY_HELLO, helloJson)
        if (helloRet != SpeechEngineDefines.ERR_NO_ERROR) {
            appendLog("播报开场白失败，返回值: $helloRet")
        } else {
            appendLog("播报开场白指令发送成功")
        }
    }

    private fun stopRecording() {
        val engine = SpeechManager.getEngine()
        if (engine == null) return
        appendLog("----- 停止录音 (结束说话) -----")
        val ret = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_FINISH_TALKING, "")
        appendLog("结束说话结果: $ret")
    }
    
    private fun cancelRecording() {
        val engine = SpeechManager.getEngine()
        if (engine == null) return
        appendLog("----- 取消录音 -----")
        val ret = engine.sendDirective(SpeechEngineDefines.DIRECTIVE_STOP_ENGINE, "")
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