package com.common.speech


interface VoiceRecognitionCallback {

    fun onBeginOfSpeech()

    fun onEndOfSpeech()


    fun onPartialResult(text: String)


    fun onFinalResult(text: String)


    fun onError(errorCode: Int, errorMsg: String)


    fun onVolumeChanged(volume: Int)
}
