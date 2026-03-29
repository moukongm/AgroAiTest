package com.common.speech

import android.app.Application
import android.util.Log
import com.bytedance.speech.speechengine.SpeechEngine
import com.bytedance.speech.speechengine.SpeechEngineDefines
import com.bytedance.speech.speechengine.SpeechEngineGenerator

object SpeechManager {
    private const val TAG = "SpeechManager"
    private var engine: SpeechEngine? = null

    // 服务接口认证信息
    private const val APP_ID = "6719567355"
    private const val ACCESS_TOKEN = "tJ3r4vC1ul8oaCnbDdfSGLYZwkz7zZx0"
    private const val SECRET_KEY = "RVFJy50ApFUShN-_iBXeA3fRdbV1PWYy"
    
    fun init(application: Application) {
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
        
        // 配置为在线模式
        speechEngine.setOptionInt(SpeechEngineDefines.PARAMS_KEY_DIALOG_WORK_MODE_INT, SpeechEngineDefines.DIALOG_WORK_MODE_DEFAULT)

        // 启用内置录音机和播放器以支持实时对话
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_RECORDER_PATH_STRING, "") // 空代表使用系统麦克风
        speechEngine.setOptionString(SpeechEngineDefines.PARAMS_KEY_DIALOG_PLAYER_PATH_STRING, "") // 空代表使用系统扬声器
        // 这里必须不要设置 callback bool 为 true，否则会接管底层的数据导致无法播放和录音
        // speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_DIALOG_ENABLE_RECORDER_AUDIO_CALLBACK_BOOL, true)
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_DIALOG_ENABLE_PLAYER_BOOL, true)
        
        // 开启回声消除 (AEC)
        speechEngine.setOptionBoolean(SpeechEngineDefines.PARAMS_KEY_ENABLE_AEC_BOOL, true)
        
        // 初始化引擎
        val ret = speechEngine.initEngine()
        Log.i(TAG, "Speech engine init result: $ret")
        
        Log.i(TAG, "Speech engine setup completed")
    }
    
    fun getEngine(): SpeechEngine? {
        return engine
    }
}
