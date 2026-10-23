package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.voice.WakeWordConfig
import com.example.voice.WakeWordOption

class WakeWordPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jarvis_wake_word_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WAKE_WORD = "key_wake_word_option"
        private const val KEY_SENSITIVITY = "key_sensitivity_level"
        private const val KEY_NOISE_CANCEL = "key_noise_cancellation"
        private const val KEY_WAKE_CHIME = "key_wake_chime"
        private const val KEY_LOW_POWER = "key_low_power_sensor"
        private const val KEY_SILENCE_TIMEOUT = "key_silence_timeout"
    }

    fun loadConfig(): WakeWordConfig {
        val wakeWordId = prefs.getString(KEY_WAKE_WORD, WakeWordOption.HEY_JARVIS.id)
        val selectedOption = WakeWordOption.values().firstOrNull { it.id == wakeWordId }
            ?: WakeWordOption.HEY_JARVIS

        val sensitivity = prefs.getFloat(KEY_SENSITIVITY, 0.70f)
        val noiseCancel = prefs.getBoolean(KEY_NOISE_CANCEL, true)
        val chime = prefs.getBoolean(KEY_WAKE_CHIME, true)
        val lowPower = prefs.getBoolean(KEY_LOW_POWER, false)
        val timeout = prefs.getFloat(KEY_SILENCE_TIMEOUT, 2.0f)

        return WakeWordConfig(
            selectedWakeWord = selectedOption,
            sensitivity = sensitivity,
            noiseCancellation = noiseCancel,
            wakeChimeEnabled = chime,
            lowPowerMode = lowPower,
            autoSilenceTimeoutSeconds = timeout
        )
    }

    fun saveConfig(config: WakeWordConfig) {
        prefs.edit()
            .putString(KEY_WAKE_WORD, config.selectedWakeWord.id)
            .putFloat(KEY_SENSITIVITY, config.sensitivity)
            .putBoolean(KEY_NOISE_CANCEL, config.noiseCancellation)
            .putBoolean(KEY_WAKE_CHIME, config.wakeChimeEnabled)
            .putBoolean(KEY_LOW_POWER, config.lowPowerMode)
            .putFloat(KEY_SILENCE_TIMEOUT, config.autoSilenceTimeoutSeconds)
            .apply()
    }
}
