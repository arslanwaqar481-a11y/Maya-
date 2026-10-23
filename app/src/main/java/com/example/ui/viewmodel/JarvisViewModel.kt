package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.controller.DeviceTelemetry
import com.example.controller.JarvisPhoneController
import com.example.controller.RingerState
import com.example.data.CommandLogEntity
import com.example.data.JarvisDatabase
import com.example.data.ReminderEntity
import com.example.data.WakeWordDataStore
import com.example.voice.JarvisAction
import com.example.voice.JarvisCommandParser
import com.example.voice.JarvisSpeechRecognizer
import com.example.voice.JarvisVoiceSpeaker
import com.example.voice.WakeWordConfig
import com.example.voice.WakeWordOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WakeWordDetectionResult(
    val detectedPhrase: String = "",
    val confidenceScore: Float = 0f,
    val matchedWakeWord: WakeWordOption? = null,
    val isTriggered: Boolean = false,
    val timestamp: Long = 0L
)

data class JarvisUiState(
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val audioLevel: Float = 0f,
    val speechTranscript: String = "",
    val jarvisResponse: String = "JARVIS v3.8 Core Online. Standing by for voice or tactile directives.",
    val lastAction: String = "IDLE",
    val isTorchOn: Boolean = false,
    val ringerState: RingerState = RingerState.NORMAL,
    val telemetry: DeviceTelemetry = DeviceTelemetry(),
    val directInputText: String = "",
    val statusMessage: String? = null,
    val wakeWordConfig: WakeWordConfig = WakeWordConfig(),
    val lastWakeDetection: WakeWordDetectionResult? = null
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JarvisDatabase.getDatabase(application)
    private val reminderDao = db.reminderDao()
    private val logDao = db.commandLogDao()

    val phoneController = JarvisPhoneController(application)
    private val wakeWordDataStore = WakeWordDataStore(application)

    private val _uiState = MutableStateFlow(
        JarvisUiState(
            isTorchOn = phoneController.isTorchEnabled(),
            ringerState = phoneController.getRingerMode(),
            telemetry = phoneController.getTelemetry()
        )
    )
    val uiState: StateFlow<JarvisUiState> = _uiState.asStateFlow()

    val reminders: StateFlow<List<ReminderEntity>> = reminderDao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val commandLogs: StateFlow<List<CommandLogEntity>> = logDao.getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var voiceSpeaker: JarvisVoiceSpeaker? = null
    private var speechRecognizer: JarvisSpeechRecognizer? = null

    init {
        voiceSpeaker = JarvisVoiceSpeaker(application) { speaking ->
            _uiState.value = _uiState.value.copy(isSpeaking = speaking)
        }

        // Collect persisted wake-word and sensitivity configurations from DataStore
        viewModelScope.launch(Dispatchers.IO) {
            wakeWordDataStore.wakeWordConfigFlow.collect { persistedConfig ->
                _uiState.value = _uiState.value.copy(wakeWordConfig = persistedConfig)
                voiceSpeaker?.updateVoiceParameters(
                    persistedConfig.selectedWakeWord.voicePitch,
                    persistedConfig.selectedWakeWord.voiceRate
                )
            }
        }

        speechRecognizer = JarvisSpeechRecognizer(
            context = application,
            onResult = { text ->
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    speechTranscript = text,
                    audioLevel = 0f
                )
                executeCommand(text)
            },
            onPartialResult = { partial ->
                _uiState.value = _uiState.value.copy(speechTranscript = partial)
            },
            onRmsChanged = { rms ->
                _uiState.value = _uiState.value.copy(audioLevel = rms)
            },
            onListeningStateChanged = { listening ->
                _uiState.value = _uiState.value.copy(isListening = listening)
            },
            onError = { errMsg ->
                _uiState.value = _uiState.value.copy(
                    isListening = false,
                    audioLevel = 0f,
                    statusMessage = errMsg
                )
            }
        )

        refreshTelemetry()
    }

    // Wake-Word & Offline Sensitivity Configuration using DataStore persistence
    fun selectWakeWord(option: WakeWordOption) {
        val updated = _uiState.value.wakeWordConfig.copy(selectedWakeWord = option)
        _uiState.value = _uiState.value.copy(
            wakeWordConfig = updated,
            jarvisResponse = option.voiceGreeting
        )
        voiceSpeaker?.updateVoiceParameters(option.voicePitch, option.voiceRate)
        speak(option.voiceGreeting)
        viewModelScope.launch(Dispatchers.IO) {
            wakeWordDataStore.saveSelectedWakeWord(option)
        }
    }

    fun setSensitivity(level: Float) {
        val clamped = level.coerceIn(0.10f, 1.0f)
        val updated = _uiState.value.wakeWordConfig.copy(sensitivity = clamped)
        _uiState.value = _uiState.value.copy(wakeWordConfig = updated)
        viewModelScope.launch(Dispatchers.IO) {
            wakeWordDataStore.saveSensitivity(clamped)
        }
    }

    fun setNoiseCancellation(enabled: Boolean) {
        val updated = _uiState.value.wakeWordConfig.copy(noiseCancellation = enabled)
        _uiState.value = _uiState.value.copy(wakeWordConfig = updated)
        viewModelScope.launch(Dispatchers.IO) {
            wakeWordDataStore.saveNoiseCancellation(enabled)
        }
    }

    fun setWakeChime(enabled: Boolean) {
        val updated = _uiState.value.wakeWordConfig.copy(wakeChimeEnabled = enabled)
        _uiState.value = _uiState.value.copy(wakeWordConfig = updated)
        viewModelScope.launch(Dispatchers.IO) {
            wakeWordDataStore.saveWakeChime(enabled)
        }
    }

    fun setLowPowerMode(enabled: Boolean) {
        val updated = _uiState.value.wakeWordConfig.copy(lowPowerMode = enabled)
        _uiState.value = _uiState.value.copy(wakeWordConfig = updated)
        viewModelScope.launch(Dispatchers.IO) {
            wakeWordDataStore.saveLowPowerMode(enabled)
        }
    }

    fun setSilenceTimeout(seconds: Float) {
        val updated = _uiState.value.wakeWordConfig.copy(autoSilenceTimeoutSeconds = seconds)
        _uiState.value = _uiState.value.copy(wakeWordConfig = updated)
        viewModelScope.launch(Dispatchers.IO) {
            wakeWordDataStore.saveSilenceTimeout(seconds)
        }
    }

    fun testPersonaVoice(option: WakeWordOption) {
        voiceSpeaker?.updateVoiceParameters(option.voicePitch, option.voiceRate)
        speak(option.voiceGreeting)
    }

    fun simulateWakeWordTest(phrase: String) {
        val activeConfig = _uiState.value.wakeWordConfig
        val activeWake = activeConfig.selectedWakeWord
        val lower = phrase.lowercase().trim()

        val matchesActive = activeWake.defaultKeywords.any { kw -> lower.contains(kw) }
        val matchesOther = WakeWordOption.values().firstOrNull { it != activeWake }?.defaultKeywords?.any { kw -> lower.contains(kw) } == true

        val rawConfidence = when {
            matchesActive -> (0.85f + (activeConfig.sensitivity * 0.14f)).coerceAtMost(0.99f)
            matchesOther -> 0.40f
            lower.contains("jarvis") || lower.contains("maya") -> 0.72f
            else -> (0.20f + (activeConfig.sensitivity * 0.25f)).coerceAtMost(0.55f)
        }

        val requiredThreshold = 1.0f - (activeConfig.sensitivity * 0.45f)
        val isTriggered = rawConfidence >= requiredThreshold && matchesActive

        val result = WakeWordDetectionResult(
            detectedPhrase = phrase,
            confidenceScore = rawConfidence,
            matchedWakeWord = if (matchesActive) activeWake else if (matchesOther) WakeWordOption.values().first { it != activeWake } else null,
            isTriggered = isTriggered,
            timestamp = System.currentTimeMillis()
        )

        _uiState.value = _uiState.value.copy(lastWakeDetection = result)

        if (isTriggered) {
            speak("${activeWake.displayName} wake-word verified. Sensitivity passed.")
        } else {
            speak("Acoustic sample below threshold (${(rawConfidence * 100).toInt()}% vs required ${(requiredThreshold * 100).toInt()}%).")
        }
    }

    fun startVoiceListening() {
        _uiState.value = _uiState.value.copy(
            speechTranscript = "Awaiting acoustic input...",
            statusMessage = null
        )
        speechRecognizer?.startListening()
    }

    fun stopVoiceListening() {
        speechRecognizer?.stopListening()
        _uiState.value = _uiState.value.copy(isListening = false, audioLevel = 0f)
    }

    fun updateDirectInput(text: String) {
        _uiState.value = _uiState.value.copy(directInputText = text)
    }

    fun executeDirectInput() {
        val query = _uiState.value.directInputText.trim()
        if (query.isNotBlank()) {
            _uiState.value = _uiState.value.copy(directInputText = "")
            executeCommand(query)
        }
    }

    fun executeCommand(rawCommand: String) {
        val activeWake = _uiState.value.wakeWordConfig.selectedWakeWord
        val parsed = JarvisCommandParser.parse(rawCommand, activeWake)
        var responseText = parsed.spokenResponse

        when (val action = parsed.action) {
            is JarvisAction.ToggleTorch -> {
                val ok = phoneController.toggleTorch(action.enable)
                _uiState.value = _uiState.value.copy(isTorchOn = phoneController.isTorchEnabled())
                if (!ok) responseText = "Unable to access optical hardware."
            }
            is JarvisAction.Call -> {
                val ok = phoneController.makeCall(action.target)
                if (!ok) responseText = "Communication hardware did not respond."
            }
            is JarvisAction.SendWhatsApp -> {
                phoneController.sendWhatsApp(action.recipient, action.message)
            }
            is JarvisAction.SendSms -> {
                phoneController.sendSms(action.recipient, action.message)
            }
            is JarvisAction.LaunchApp -> {
                val result = phoneController.launchAppByName(action.appName)
                responseText = result.message
            }
            is JarvisAction.AdjustVolume -> {
                phoneController.adjustVolume(action.increase)
            }
            is JarvisAction.SetRinger -> {
                val mode = when (action.mode) {
                    "SILENT" -> RingerState.SILENT
                    "VIBRATE" -> RingerState.VIBRATE
                    else -> RingerState.NORMAL
                }
                phoneController.setRingerMode(mode)
                _uiState.value = _uiState.value.copy(ringerState = mode)
            }
            is JarvisAction.CheckBattery -> {
                val tele = phoneController.getTelemetry()
                val chargeMsg = if (tele.isCharging) "Power conduits connected and replenishing." else "Running on internal cells."
                val honorific = if (activeWake == WakeWordOption.MAYA) "" else ", Sir"
                responseText = "Battery level is at ${tele.batteryPercent} percent$honorific. $chargeMsg"
            }
            is JarvisAction.AddReminder -> {
                addReminder(action.title, action.time, "Voice Directive", "High")
            }
            is JarvisAction.ReadSchedule -> {
                readScheduleVoice()
                return
            }
            is JarvisAction.MorningBriefing -> {
                triggerMorningBriefing()
                return
            }
            is JarvisAction.OpenWifiSettings -> {
                phoneController.openWifiSettings()
            }
            is JarvisAction.OpenBluetoothSettings -> {
                phoneController.openBluetoothSettings()
            }
            is JarvisAction.Conversation -> {
                responseText = action.reply
            }
            is JarvisAction.Unknown -> {
                // Kept as parsed
            }
        }

        _uiState.value = _uiState.value.copy(
            jarvisResponse = responseText,
            lastAction = parsed.actionLabel
        )

        speak(responseText)

        // Log to database
        viewModelScope.launch(Dispatchers.IO) {
            logDao.insertLog(
                CommandLogEntity(
                    commandText = rawCommand,
                    responseText = responseText,
                    actionType = parsed.actionLabel
                )
            )
        }

        refreshTelemetry()
    }

    fun speak(text: String) {
        voiceSpeaker?.speak(text)
    }

    fun stopSpeaking() {
        voiceSpeaker?.stop()
    }

    fun toggleTorch() {
        val next = !_uiState.value.isTorchOn
        phoneController.toggleTorch(next)
        _uiState.value = _uiState.value.copy(isTorchOn = phoneController.isTorchEnabled())
        val honorific = if (_uiState.value.wakeWordConfig.selectedWakeWord == WakeWordOption.MAYA) "" else ", Sir"
        val msg = if (next) "Torch illuminated$honorific." else "Torch turned off$honorific."
        _uiState.value = _uiState.value.copy(jarvisResponse = msg, lastAction = "TORCH_TOGGLE")
        speak(msg)
    }

    fun setRingerMode(mode: RingerState) {
        phoneController.setRingerMode(mode)
        _uiState.value = _uiState.value.copy(ringerState = mode)
        val honorific = if (_uiState.value.wakeWordConfig.selectedWakeWord == WakeWordOption.MAYA) "" else ", Sir"
        val msg = "Acoustic mode switched to ${mode.name.lowercase()}$honorific."
        _uiState.value = _uiState.value.copy(jarvisResponse = msg, lastAction = "RINGER_${mode.name}")
        speak(msg)
    }

    fun adjustVolume(increase: Boolean) {
        phoneController.adjustVolume(increase)
        val dir = if (increase) "increased" else "decreased"
        val honorific = if (_uiState.value.wakeWordConfig.selectedWakeWord == WakeWordOption.MAYA) "" else ", Sir"
        val msg = "Volume $dir$honorific."
        _uiState.value = _uiState.value.copy(jarvisResponse = msg, lastAction = "VOLUME_ADJUST")
        speak(msg)
    }

    fun readScheduleVoice() {
        val list = reminders.value.filter { !it.isCompleted }
        val honorific = if (_uiState.value.wakeWordConfig.selectedWakeWord == WakeWordOption.MAYA) "" else ", Sir"
        val msg = if (list.isEmpty()) {
            "You have no pending items on today's agenda$honorific. All protocols clear."
        } else {
            val items = list.take(3).joinToString("; ") { "${it.title} at ${it.timeLabel}" }
            "You have ${list.size} pending tasks on your schedule$honorific: $items."
        }
        _uiState.value = _uiState.value.copy(jarvisResponse = msg, lastAction = "READ_SCHEDULE")
        speak(msg)
    }

    fun triggerMorningBriefing() {
        val tele = phoneController.getTelemetry()
        val pendingCount = reminders.value.count { !it.isCompleted }
        val honorific = if (_uiState.value.wakeWordConfig.selectedWakeWord == WakeWordOption.MAYA) "" else ", Sir"
        val msg = "Good morning$honorific. Core battery is at ${tele.batteryPercent} percent. Memory usage is optimal at ${tele.ramUsedMb} megabytes. You have $pendingCount operational items on your schedule. All systems prepared."
        _uiState.value = _uiState.value.copy(jarvisResponse = msg, lastAction = "MORNING_BRIEFING")
        speak(msg)
    }

    fun addReminder(title: String, timeLabel: String, category: String, priority: String) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.insertReminder(
                ReminderEntity(
                    title = title,
                    timeLabel = timeLabel,
                    category = category,
                    priority = priority,
                    isCompleted = false
                )
            )
        }
    }

    fun toggleReminder(reminder: ReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.updateReminder(reminder.copy(isCompleted = !reminder.isCompleted))
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.deleteReminder(reminder)
        }
    }

    fun clearCompletedReminders() {
        viewModelScope.launch(Dispatchers.IO) {
            reminderDao.clearCompleted()
        }
    }

    fun refreshTelemetry() {
        val tele = phoneController.getTelemetry()
        _uiState.value = _uiState.value.copy(telemetry = tele)
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer?.destroy()
        voiceSpeaker?.shutdown()
    }
}
