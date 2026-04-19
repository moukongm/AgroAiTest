package com.common.speech

import android.app.Application
import android.util.Log
import com.bytedance.speech.speechengine.SpeechEngine
import com.bytedance.speech.speechengine.SpeechEngineDefines
import com.bytedance.speech.speechengine.SpeechEngineGenerator
import java.io.File

object SpeechManager {
    private const val TAG = "SpeechManager"
    private const val AEC_MODEL_ASSET_NAME = "aec.model"
    private var engine: SpeechEngine? = null
    private lateinit var application: Application

    // 服务接口认证信息
    private const val APP_ID = "6719567355"
    private const val ACCESS_TOKEN = "tJ3r4vC1ul8oaCnbDdfSGLYZwkz7zZx0"
    // TODO: 使用日志中服务端期望的正确 AppKey
    private const val SECRET_KEY = "PlgvMymc7f3tQnJ6"
    
    fun init(application: Application) {
        this.application = application
        try {
            SpeechEngineGenerator.PrepareEnvironment(application.applicationContext, application)
            engine = SpeechEngineGenerator.getInstance()
            engine?.createEngine()
            
            setupEngine()
        } catch (e: Exception) {
            Log.e(TAG, "Init speech engine failed", e)
        }
    }
    
    private fun setupEngine() {
        val speechEngine = engine ?: return
        
        //【必需配置】Engine Name
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_ENGINE_NAME_STRING, SpeechEngineDefines.DIALOG_ENGINE)
        
        //【必需配置】鉴权相关：Appid
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_ID_STRING, APP_ID)
        //【必需配置】鉴权相关：AppKey
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_KEY_STRING, SECRET_KEY)
        //【必需配置】鉴权相关：Token
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_APP_TOKEN_STRING, ACCESS_TOKEN)
        
        //【必需配置】对话服务资源信息ResourceId
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_RESOURCE_ID_STRING, "volc.speech.dialog")
        
        //【必需配置】User ID
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_UID_STRING, "uid_agro_ai")

        // 【必需配置】地址相关，不能使用 wss://，必须显式传递并使用 https:// 协议头，底层 OkHttpWsClient 会进行转换
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_ADDRESS_STRING, "https://openspeech.bytedance.com")
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_URI_STRING, "/api/v3/realtime/dialogue")
        
        // 配置日志路径
        val logPath = application.getExternalFilesDir("speech_log")?.absolutePath ?: ""
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DEBUG_PATH_STRING, logPath)
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_LOG_LEVEL_STRING, SpeechEngineDefines.LOG_LEVEL_TRACE)

        // 配置为在线模式
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_DIALOG_WORK_MODE_INT, SpeechEngineDefines.DIALOG_WORK_MODE_DEFAULT)

        // 启用内置录音机和播放器以支持实时对话
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_RECORDER_TYPE_STRING, SpeechEngineDefines.RECORDER_TYPE_RECORDER)
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_RECORDER_PATH_STRING, "") // 空代表使用系统麦克风
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_PLAYER_PATH_STRING, "") // 空代表使用系统扬声器
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_DIALOG_ENABLE_PLAYER_BOOL, true)
        
        // 【关键】告知引擎：自动连接并启动会话
        // speechEngine.setOptionBoolean("dialog_enable_auto_connection", true)
        // speechEngine.setOptionBoolean("dialog_enable_auto_session", true)

        val aecModelPath = ensureAecModelPath()
        if (aecModelPath != null) {
            speechEngine.setOptionString(
                SpeechEngineDefines.PARAMS_KEY_AEC_MODEL_PATH_STRING,
                aecModelPath
            )
            speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_ENABLE_AEC_BOOL, true)
            Log.i(TAG, "AEC enabled with model path: $aecModelPath")
        } else {
            speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_ENABLE_AEC_BOOL, false)
            Log.w(TAG, "AEC model unavailable, keep AEC disabled")
        }
        
        // 初始化引擎
        val ret = speechEngine.initEngine()
        Log.i(TAG, "Speech engine init result: $ret")
        
        Log.i(TAG, "Speech engine setup completed")
    }
    
    fun getEngine(): SpeechEngine? {
        return engine
    }

    private fun ensureAecModelPath(): String? {
        return try {
            val targetDir = File(application.filesDir, "speech_models")
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                Log.e(TAG, "Failed to create speech model directory: ${targetDir.absolutePath}")
                return null
            }

            val targetFile = File(targetDir, AEC_MODEL_ASSET_NAME)
            val shouldRewrite = !targetFile.exists() || targetFile.length() == 0L
            if (shouldRewrite) {
                application.assets.open(AEC_MODEL_ASSET_NAME).use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }

            if (targetFile.exists() && targetFile.length() > 0L) {
                targetFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Prepare aec.model failed", e)
            null
        }
    }
}
