package com.example.sound

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

class KeyboardTtsEngine private constructor(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    var isSpeaking = false
        private set

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    try {
                        tts?.language = Locale("ar")
                    } catch (_: Exception) {}

                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            isSpeaking = true
                        }
                        override fun onDone(utteranceId: String?) {
                            isSpeaking = false
                        }
                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            isSpeaking = false
                        }
                    })
                }
            }
        } catch (_: Exception) {
            isInitialized = false
        }
    }

    fun speak(text: String, isArabic: Boolean = true, speedRate: Float = 1.0f) {
        if (text.isBlank()) return
        val currentTts = tts ?: return
        try {
            val locale = if (isArabic) Locale("ar") else Locale("en")
            currentTts.language = locale
            currentTts.setSpeechRate(speedRate.coerceIn(0.5f, 2.0f))
            currentTts.setPitch(1.0f)
            val utteranceId = "TURBO_TTS_${System.currentTimeMillis()}"
            currentTts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            isSpeaking = true
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
        isSpeaking = false
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
        tts = null
        isInitialized = false
        isSpeaking = false
    }

    companion object {
        @Volatile
        private var instance: KeyboardTtsEngine? = null

        fun getInstance(context: Context): KeyboardTtsEngine {
            return instance ?: synchronized(this) {
                instance ?: KeyboardTtsEngine(context).also { instance = it }
            }
        }
    }
}
