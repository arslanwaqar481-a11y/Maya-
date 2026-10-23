package com.example.voice

sealed class JarvisAction {
    data class Call(val target: String) : JarvisAction()
    data class SendSms(val recipient: String, val message: String) : JarvisAction()
    data class SendWhatsApp(val recipient: String?, val message: String) : JarvisAction()
    data class LaunchApp(val appName: String) : JarvisAction()
    data class ToggleTorch(val enable: Boolean) : JarvisAction()
    data class AdjustVolume(val increase: Boolean) : JarvisAction()
    data class SetRinger(val mode: String) : JarvisAction()
    object CheckBattery : JarvisAction()
    data class AddReminder(val title: String, val time: String) : JarvisAction()
    object ReadSchedule : JarvisAction()
    object MorningBriefing : JarvisAction()
    object OpenWifiSettings : JarvisAction()
    object OpenBluetoothSettings : JarvisAction()
    data class Conversation(val reply: String) : JarvisAction()
    object Unknown : JarvisAction()
}

data class ParsedCommand(
    val rawText: String,
    val action: JarvisAction,
    val spokenResponse: String,
    val actionLabel: String
)

object JarvisCommandParser {

    fun parse(input: String, wakeWordOption: WakeWordOption = WakeWordOption.HEY_JARVIS): ParsedCommand {
        val trimmed = input.trim().lowercase()

        // Check if input is only a wake word call
        val isJustWakeWord = listOf("hey jarvis", "jarvis", "ok jarvis", "hey maya", "maya", "ok maya").contains(trimmed)
        if (isJustWakeWord) {
            val isMaya = trimmed.contains("maya")
            val greeting = if (isMaya) WakeWordOption.MAYA.voiceGreeting else WakeWordOption.HEY_JARVIS.voiceGreeting
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation(greeting),
                spokenResponse = greeting,
                actionLabel = if (isMaya) "WAKE_MAYA_TRIGGERED" else "WAKE_JARVIS_TRIGGERED"
            )
        }

        // Strip wake-word prefix if present
        var body = trimmed
        listOf("hey jarvis", "jarvis", "ok jarvis", "hey maya", "maya", "ok maya").forEach { kw ->
            if (body.startsWith("$kw ")) {
                body = body.removePrefix("$kw ").trim()
            }
        }
        val clean = body
        val honorific = if (wakeWordOption == WakeWordOption.MAYA) "" else ", Sir"

        // 1. Flashlight / Torch
        if (clean.contains("torch on") || clean.contains("flashlight on") ||
            clean.contains("light on") || clean.contains("light jalao") || clean.contains("flash on")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.ToggleTorch(true),
                spokenResponse = if (wakeWordOption == WakeWordOption.MAYA) "Flashlight turned on." else "Auxiliary optics illuminated, Sir.",
                actionLabel = "TORCH_ON"
            )
        }

        if (clean.contains("torch off") || clean.contains("flashlight off") ||
            clean.contains("light off") || clean.contains("light band karo") || clean.contains("flash off")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.ToggleTorch(false),
                spokenResponse = if (wakeWordOption == WakeWordOption.MAYA) "Flashlight deactivated." else "Auxiliary optics deactivated, Sir.",
                actionLabel = "TORCH_OFF"
            )
        }

        // 2. Calling
        val callRegex = Regex("""(?:call|dial|phone lagao|call karo)\s+([a-zA-Z0-9+\s]+)""")
        val callMatch = callRegex.find(clean)
        if (callMatch != null) {
            val target = callMatch.groupValues[1].trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Call(target),
                spokenResponse = "Initiating voice link to $target, Sir.",
                actionLabel = "CALL_$target"
            )
        }

        // 3. WhatsApp
        val waRegex = Regex("""(?:whatsapp|send whatsapp to)\s*(?:([a-zA-Z0-9+\s]+)\s+(?:saying|message|ko))?\s*(.*)""")
        if (clean.startsWith("whatsapp") || clean.contains("send whatsapp")) {
            val msg = clean.removePrefix("whatsapp").removePrefix("send whatsapp").trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SendWhatsApp(null, if (msg.isNotBlank()) msg else "Hello from JARVIS"),
                spokenResponse = "Opening encrypted WhatsApp channel, Sir.",
                actionLabel = "WHATSAPP"
            )
        }

        // 4. SMS / Message
        if (clean.contains("send message") || clean.contains("sms") || clean.contains("message bhej")) {
            val msg = clean.replace(Regex("(send message|sms|message bhej|ko|to)"), "").trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SendSms("", if (msg.isNotBlank()) msg else "Message from JARVIS"),
                spokenResponse = "Preparing transmission protocol, Sir.",
                actionLabel = "SEND_SMS"
            )
        }

        // 5. Volume & Acoustics
        if (clean.contains("volume up") || clean.contains("awaz badhao") || clean.contains("louder")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.AdjustVolume(true),
                spokenResponse = "Amplifying audio output, Sir.",
                actionLabel = "VOLUME_UP"
            )
        }
        if (clean.contains("volume down") || clean.contains("awaz kam karo") || clean.contains("quieter")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.AdjustVolume(false),
                spokenResponse = "Attenuating audio gain, Sir.",
                actionLabel = "VOLUME_DOWN"
            )
        }
        if (clean.contains("silent") || clean.contains("mute") || clean.contains("shant")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SetRinger("SILENT"),
                spokenResponse = "Stealth audio mode engaged. Ringer silenced.",
                actionLabel = "RINGER_SILENT"
            )
        }
        if (clean.contains("normal sound") || clean.contains("unmute") || clean.contains("ringer normal")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SetRinger("NORMAL"),
                spokenResponse = "Standard acoustic frequencies restored, Sir.",
                actionLabel = "RINGER_NORMAL"
            )
        }

        // 6. Battery & Telemetry
        if (clean.contains("battery") || clean.contains("power level") || clean.contains("charge kitna") || clean.contains("energy")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.CheckBattery,
                spokenResponse = "Checking power core diagnostics, Sir.",
                actionLabel = "CHECK_BATTERY"
            )
        }

        // 7. Schedule Queries & Reminders
        if (clean.contains("what is my schedule") || clean.contains("today's agenda") ||
            clean.contains("aaj kya kaam") || clean.contains("read schedule") || clean.contains("my reminders") ||
            clean.contains("show schedule") || clean == "schedule"
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.ReadSchedule,
                spokenResponse = "Accessing your tactical itinerary, Sir.",
                actionLabel = "READ_SCHEDULE"
            )
        }

        if (clean.contains("remind me") || clean.contains("add reminder") || clean.contains("yaad dilana") || clean.startsWith("schedule ")) {
            val task = clean
                .replace("remind me to", "")
                .replace("remind me", "")
                .replace("add reminder", "")
                .replace("yaad dilana", "")
                .replace("schedule", "")
                .trim()
            val finalTask = if (task.isNotBlank()) task.replaceFirstChar { it.uppercase() } else "New Directive"
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.AddReminder(finalTask, "Scheduled"),
                spokenResponse = "Affirmative, Sir. Added '$finalTask' to your schedule.",
                actionLabel = "ADD_REMINDER"
            )
        }

        // 8. Morning Briefing / Diagnostics
        if (clean.contains("morning briefing") || clean.contains("good morning") ||
            clean.contains("subah ka briefing") || clean.contains("status report") || clean.contains("system status")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.MorningBriefing,
                spokenResponse = "Good morning, Sir. Core reactors are stable. All system diagnostics report nominal efficiency.",
                actionLabel = "MORNING_BRIEFING"
            )
        }

        // 9. Hardware Settings
        if (clean.contains("wifi") || clean.contains("wi-fi")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.OpenWifiSettings,
                spokenResponse = "Deploying Wi-Fi telemetry controls.",
                actionLabel = "SETTINGS_WIFI"
            )
        }
        if (clean.contains("bluetooth")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.OpenBluetoothSettings,
                spokenResponse = "Deploying Bluetooth relay configuration.",
                actionLabel = "SETTINGS_BLUETOOTH"
            )
        }

        // 10. App Launching ("open [app]", "kholna [app]", "launch [app]")
        val launchRegex = Regex("""(?:open|launch|kholo|chalu karo)\s+([a-zA-Z0-9\s]+)""")
        val launchMatch = launchRegex.find(clean)
        if (launchMatch != null) {
            val appTarget = launchMatch.groupValues[1].trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.LaunchApp(appTarget),
                spokenResponse = "Opening $appTarget right away, Sir.",
                actionLabel = "LAUNCH_$appTarget"
            )
        }

        // 11. Conversational Queries
        if (clean.contains("who are you") || clean.contains("tum kaun ho") || clean.contains("what are you")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation("I am JARVIS: Just A Rather Very Intelligent System, running entirely on your Android hardware with zero server dependency."),
                spokenResponse = "I am JARVIS: Just A Rather Very Intelligent System. Ready to execute your commands, Sir.",
                actionLabel = "QUERY_IDENTITY"
            )
        }

        if (clean.contains("hello") || clean.contains("hey jarvis") || clean.contains("namaste")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation("Online and standing by, Sir. What are your orders?"),
                spokenResponse = "Online and standing by, Sir. What are your orders?",
                actionLabel = "GREETING"
            )
        }

        if (clean.contains("thank you") || clean.contains("thanks") || clean.contains("shukriya")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation("Always at your service, Sir."),
                spokenResponse = "Always at your service, Sir.",
                actionLabel = "THANKS"
            )
        }

        // Fallback: If it's a short text like "youtube", "camera", treat as launch request
        val singleWord = clean.trim()
        val commonApps = listOf("youtube", "whatsapp", "camera", "chrome", "maps", "calculator", "settings", "clock")
        if (commonApps.contains(singleWord)) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.LaunchApp(singleWord),
                spokenResponse = "Opening $singleWord, Sir.",
                actionLabel = "LAUNCH_$singleWord"
            )
        }

        return ParsedCommand(
            rawText = input,
            action = JarvisAction.Unknown,
            spokenResponse = "I heard '$input', Sir. You can ask me to open apps, toggle the torch, call contacts, check battery, or update your schedule.",
            actionLabel = "UNKNOWN"
        )
    }
}
