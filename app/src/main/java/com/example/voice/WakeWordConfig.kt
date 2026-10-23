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
        description = "مردانہ باوقار اردو آواز۔ جامع، تیز اور موبائل کنٹرول کے لیے تیار۔",
        voiceGreeting = "جی جناب! جاروس حاضر ہے۔ فرمائیے کیا حکم ہے؟",
        voicePitch = 0.95f,
        voiceRate = 1.0f,
        defaultKeywords = listOf("hey jarvis", "jarvis", "ok jarvis", "جاروس", "ہیلو جاروس", "اوکے جاروس", "سنو جاروس")
    ),
    MAYA(
        id = "maya",
        displayName = "MAYA",
        triggerPhrase = "Maya",
        personaTitle = "Quantum Neural Persona",
        description = "تیز رفتار اور جدید زنانہ اردو آواز۔ ہر وقت رہنمائی کے لیے تیار۔",
        voiceGreeting = "مایا نیورل اسسٹنٹ آن لائن ہے۔ بتائیے میں آپ کی کیا مدد کر سکتی ہوں؟",
        voicePitch = 1.25f,
        voiceRate = 1.05f,
        defaultKeywords = listOf("hey maya", "maya", "ok maya", "مایا", "ہیلو مایا", "اوکے مایا", "سنو مایا")
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
