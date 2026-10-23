package com.example.voice

enum class WakeWordOption(
    val id: String,
    val displayName: String,
    val triggerPhrase: String,
    val personaTitle: String,
    val description: String,
    val voiceGreeting: String,
    val voicePitch: Float,
    val voiceRate: Float,
    val defaultKeywords: List<String>
) {
    HEY_JARVIS(
        id = "hey_jarvis",
        displayName = "Hey Jarvis",
        triggerPhrase = "Hey Jarvis",
        personaTitle = "Stark Tactical Core",
        description = "British male baritone persona. Formally articulated, concise, calibrated for executive tactical directives.",
        voiceGreeting = "At your command, Sir. Hey Jarvis wake-word calibrated and armed.",
        voicePitch = 0.95f,
        voiceRate = 1.05f,
        defaultKeywords = listOf("hey jarvis", "jarvis", "ok jarvis")
    ),
    MAYA(
        id = "maya",
        displayName = "MAYA",
        triggerPhrase = "Maya",
        personaTitle = "Quantum Neural Persona",
        description = "Adaptive high-speed female AI assistant. Multilingual phonetics with rapid natural language execution.",
        voiceGreeting = "MAYA neural matrix online. Listening for your voice directives.",
        voicePitch = 1.25f,
        voiceRate = 1.10f,
        defaultKeywords = listOf("hey maya", "maya", "ok maya")
    )
}

data class WakeWordConfig(
    val selectedWakeWord: WakeWordOption = WakeWordOption.HEY_JARVIS,
    val sensitivity: Float = 0.70f, // 0.1 .. 1.0
    val noiseCancellation: Boolean = true,
    val wakeChimeEnabled: Boolean = true,
    val lowPowerMode: Boolean = false,
    val autoSilenceTimeoutSeconds: Float = 2.0f
)
