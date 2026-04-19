package com.common.speech

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.common.utils.LogUtils
import com.iflytek.cloud.*
import org.json.JSONObject


object VoiceRecognitionManager {

    private const val TAG = "VoiceRecognition"

    private const val APP_ID = "17dcfcb4"

    private var mRecognizer: SpeechRecognizer? = null
    private var callback: VoiceRecognitionCallback? = null
    private var isListening = false
    private var context: Context? = null

    val isInitialized: Boolean
        get() = mRecognizer != null

    val isRecognizing: Boolean
        get() = isListening


    fun init(ctx: Context) {
        if (mRecognizer != null) {
            LogUtils.d(TAG, "语音识别引擎已初始化")
            return
        }

        this.context = ctx.applicationContext
        LogUtils.d(TAG, "开始初始化语音识别引擎，AppID: $APP_ID")

        // 创建语音配置对象，格式必须是 "appid=你的AppID"
        SpeechUtility.createUtility(ctx, SpeechConstant.APPID + "=" + APP_ID)
        LogUtils.d(TAG, "SpeechUtility 创建完成")

        // 创建语音听写对象
        mRecognizer = SpeechRecognizer.createRecognizer(ctx) { code ->
            when (code) {
                ErrorCode.SUCCESS -> Log.d(TAG, "语音识别引擎初始化成功")
                else -> LogUtils.e(TAG, "语音识别引擎初始化失败，错误码：$code")
            }
        }

        if (mRecognizer != null) {
            LogUtils.d(TAG, "VoiceRecognitionManager 初始化完成，mRecognizer 创建成功")
        } else {
            LogUtils.e(TAG, "VoiceRecognitionManager 初始化失败，mRecognizer 为 null")
        }
    }

    fun setCallback(callback: VoiceRecognitionCallback?) {
        this.callback = callback
    }


    fun startListening(ctx: Context) {
        if (mRecognizer == null) {
            callback?.onError(-1, "语音识别引擎未初始化")
            return
        }

        if (isListening) {
            stopListening()
        }

        // 保存 context
        this.context = ctx.applicationContext

        // 设置听写参数
        mRecognizer?.apply {
            // 设置语言：中文
            setParameter(SpeechConstant.LANGUAGE, "zh_cn")
            // 设置口音：普通话
            setParameter(SpeechConstant.ACCENT, "mandarin")
            // 设置返回格式：json
            setParameter(SpeechConstant.RESULT_TYPE, "json")
            // 设置前端静音超时时间：5000ms
            setParameter(SpeechConstant.VAD_BOS, "5000")
            // 设置后端静音超时时间：2000ms
            setParameter(SpeechConstant.VAD_EOS, "2000")
            // 设置标点符号：0表示无标点
            setParameter(SpeechConstant.ASR_PTT, "1")
            // 设置采样率：16000
            setParameter(SpeechConstant.SAMPLE_RATE, "16000")
        }

        isListening = true

        // 开始听写
        mRecognizer?.startListening(object : RecognizerListener {

            override fun onBeginOfSpeech() {
                Log.d(TAG, "开始说话")
                callback?.onBeginOfSpeech()
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "结束说话")
                callback?.onEndOfSpeech()
            }

            override fun onResult(result: RecognizerResult?, isLast: Boolean) {
                result?.let {
                    val text = parseResult(it.resultString)
                    if (isLast) {
                        callback?.onFinalResult(text)
                    } else {
                        callback?.onPartialResult(text)
                    }
                }
            }

            override fun onError(error: SpeechError?) {
                isListening = false
                error?.let {
                    callback?.onError(it.errorCode, it.message ?: "")
                }
            }

            override fun onVolumeChanged(volume: Int, data: ByteArray?) {
                // volume 范围 0-30
                val percent = (volume * 100 / 30).coerceIn(0, 100)
                callback?.onVolumeChanged(percent)
            }

            override fun onEvent(eventType: Int, arg1: Int, arg2: Int, obj: Bundle?) {

            }
        })
    }

    fun stopListening() {
        if (isListening && mRecognizer != null) {
            mRecognizer?.stopListening()
            isListening = false
        }
    }


    fun cancelListening() {
        if (mRecognizer != null) {
            mRecognizer?.cancel()
            isListening = false
        }
    }

    private fun parseResult(json: String): String {
        return try {
            val obj = JSONObject(json)
            val ws = obj.optJSONArray("ws")
            if (ws != null) {
                val sb = StringBuilder()
                for (i in 0 until ws.length()) {
                    val cw = ws.getJSONObject(i).optJSONArray("cw")
                    if (cw != null && cw.length() > 0) {
                        sb.append(cw.getJSONObject(0).optString("w"))
                    }
                }
                sb.toString()
            } else {
                json
            }
        } catch (e: Exception) {
            json
        }
    }


    fun release() {
        cancelListening()
        mRecognizer?.destroy()
        mRecognizer = null
        context = null
        callback = null
    }
}
