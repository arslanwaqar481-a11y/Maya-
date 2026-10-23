package com.example.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class JarvisVoiceSpeaker(
    private val context: Context,
    private val onSpeakingStateChanged: (Boolean) -> Unit = {}
) : TextToSpeech.OnInitListener {

    private val TAG = "JarvisVoiceSpeaker"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: String? = null

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.UK)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to English US or Default
                tts?.setLanguage(Locale.US)
            }

            // Refined Paul Bettany / JARVIS pitch and cadence
            tts?.setPitch(0.95f)
            tts?.setSpeechRate(1.05f)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    onSpeakingStateChanged(true)
                }

                override fun onDone(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeakingStateChanged(false)
                }
            })

            isInitialized = true
            pendingSpeech?.let {
                speak(it)
                pendingSpeech = null
            }
        } else {
            Log.e(TAG, "TextToSpeech init failed")
        }
    }

    fun speak(text: String, flush: Boolean = true) {
        if (!isInitialized) {
            pendingSpeech = text
            return
        }
        val queueMode = if (flush) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = "JARVIS_${System.currentTimeMillis()}"
        tts?.speak(text, queueMode, null, utteranceId)
    }

    fun updateVoiceParameters(pitch: Float, rate: Float) {
        tts?.setPitch(pitch)
        tts?.setSpeechRate(rate)
    }

    fun stop() {
        tts?.stop()
        onSpeakingStateChanged(false)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
