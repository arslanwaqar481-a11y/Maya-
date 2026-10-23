package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.voice.WakeWordConfig
import com.example.voice.WakeWordOption
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.wakeWordDataStore: DataStore<Preferences> by preferencesDataStore(name = "wake_word_settings")

class WakeWordDataStore(private val context: Context) {

    companion object {
        val KEY_WAKE_WORD_ID = stringPreferencesKey("wake_word_id")
        val KEY_SENSITIVITY = floatPreferencesKey("sensitivity_level")
        val KEY_NOISE_CANCEL = booleanPreferencesKey("noise_cancellation")
        val KEY_WAKE_CHIME = booleanPreferencesKey("wake_chime_enabled")
        val KEY_LOW_POWER = booleanPreferencesKey("low_power_mode")
        val KEY_SILENCE_TIMEOUT = floatPreferencesKey("silence_timeout_seconds")
    }

    val wakeWordConfigFlow: Flow<WakeWordConfig> = context.wakeWordDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val wakeWordId = preferences[KEY_WAKE_WORD_ID] ?: WakeWordOption.HEY_JARVIS.id
            val selectedOption = WakeWordOption.values().firstOrNull { it.id == wakeWordId }
                ?: WakeWordOption.HEY_JARVIS
            val sensitivity = preferences[KEY_SENSITIVITY] ?: 0.70f
            val noiseCancel = preferences[KEY_NOISE_CANCEL] ?: true
            val chime = preferences[KEY_WAKE_CHIME] ?: true
            val lowPower = preferences[KEY_LOW_POWER] ?: false
            val timeout = preferences[KEY_SILENCE_TIMEOUT] ?: 2.0f

            WakeWordConfig(
                selectedWakeWord = selectedOption,
                sensitivity = sensitivity,
                noiseCancellation = noiseCancel,
                wakeChimeEnabled = chime,
                lowPowerMode = lowPower,
                autoSilenceTimeoutSeconds = timeout
            )
        }

    suspend fun saveSelectedWakeWord(option: WakeWordOption) {
        context.wakeWordDataStore.edit { preferences ->
            preferences[KEY_WAKE_WORD_ID] = option.id
        }
    }

    suspend fun saveSensitivity(sensitivity: Float) {
        context.wakeWordDataStore.edit { preferences ->
            preferences[KEY_SENSITIVITY] = sensitivity.coerceIn(0.10f, 1.0f)
        }
    }

    suspend fun saveNoiseCancellation(enabled: Boolean) {
        context.wakeWordDataStore.edit { preferences ->
            preferences[KEY_NOISE_CANCEL] = enabled
        }
    }

    suspend fun saveWakeChime(enabled: Boolean) {
        context.wakeWordDataStore.edit { preferences ->
            preferences[KEY_WAKE_CHIME] = enabled
        }
    }

    suspend fun saveLowPowerMode(enabled: Boolean) {
        context.wakeWordDataStore.edit { preferences ->
            preferences[KEY_LOW_POWER] = enabled
        }
    }

    suspend fun saveSilenceTimeout(seconds: Float) {
        context.wakeWordDataStore.edit { preferences ->
            preferences[KEY_SILENCE_TIMEOUT] = seconds
        }
    }

    suspend fun saveFullConfig(config: WakeWordConfig) {
        context.wakeWordDataStore.edit { preferences ->
            preferences[KEY_WAKE_WORD_ID] = config.selectedWakeWord.id
            preferences[KEY_SENSITIVITY] = config.sensitivity.coerceIn(0.10f, 1.0f)
            preferences[KEY_NOISE_CANCEL] = config.noiseCancellation
            preferences[KEY_WAKE_CHIME] = config.wakeChimeEnabled
            preferences[KEY_LOW_POWER] = config.lowPowerMode
            preferences[KEY_SILENCE_TIMEOUT] = config.autoSilenceTimeoutSeconds
        }
    }
}
