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

        // 1. Check if input is solely a wake-word trigger (English, Roman Urdu, or Urdu script)
        val jarvisWakeKeywords = listOf(
            "hey jarvis", "jarvis", "ok jarvis",
            "ہیلو جاروس", "جاروس", "اوکے جاروس", "سنو جاروس", "اے جاروس"
        )
        val mayaWakeKeywords = listOf(
            "hey maya", "maya", "ok maya",
            "ہیلو مایا", "مایا", "اوکے مایا", "سنو مایا", "اے مایا"
        )

        val isJarvisOnly = jarvisWakeKeywords.contains(trimmed)
        val isMayaOnly = mayaWakeKeywords.contains(trimmed)

        if (isJarvisOnly || isMayaOnly) {
            val greeting = if (isMayaOnly) WakeWordOption.MAYA.voiceGreeting else WakeWordOption.HEY_JARVIS.voiceGreeting
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation(greeting),
                spokenResponse = greeting,
                actionLabel = if (isMayaOnly) "WAKE_MAYA_TRIGGERED" else "WAKE_JARVIS_TRIGGERED"
            )
        }

        // 2. Strip wake-word prefixes if user said e.g. "Jarvis torch on" or "جاروس ٹارچ جلاؤ"
        var body = trimmed
        (jarvisWakeKeywords + mayaWakeKeywords).forEach { kw ->
            if (body.startsWith("$kw ")) {
                body = body.removePrefix("$kw ").trim()
            }
        }
        val clean = body
        val honorific = if (wakeWordOption == WakeWordOption.MAYA) "" else "، جناب"

        // 3. Flashlight / Torch (آن / آف)
        if (clean.contains("torch on") || clean.contains("flashlight on") || clean.contains("light on") ||
            clean.contains("light jalao") || clean.contains("flash on") ||
            clean.contains("ٹارچ آن") || clean.contains("ٹارچ جلاؤ") || clean.contains("ٹارچ جلا دو") ||
            clean.contains("لائٹ جلاؤ") || clean.contains("لائٹ آن") || clean.contains("روشنی کرو") ||
            clean.contains("ٹارچ چالو") || clean.contains("torch jalao")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.ToggleTorch(true),
                spokenResponse = "ٹارچ آن کر دی گئی ہے$honorific۔",
                actionLabel = "TORCH_ON"
            )
        }

        if (clean.contains("torch off") || clean.contains("flashlight off") || clean.contains("light off") ||
            clean.contains("light band karo") || clean.contains("flash off") ||
            clean.contains("ٹارچ بند") || clean.contains("ٹارچ آف") || clean.contains("لائٹ بند") ||
            clean.contains("لائٹ آف") || clean.contains("روشنی بند") || clean.contains("torch band karo")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.ToggleTorch(false),
                spokenResponse = "ٹارچ بند کر دی گئی ہے$honorific۔",
                actionLabel = "TORCH_OFF"
            )
        }

        // 4. Calling (کال ملانا)
        val callRegex = Regex("""(?:call|dial|phone lagao|call karo|کال کرو|فون کرو|کال ملاؤ|فون لگاؤ)\s+([a-zA-Z0-9+\s\u0600-\u06FF]+)""")
        val callMatch = callRegex.find(clean)
        if (callMatch != null) {
            val target = callMatch.groupValues[1].trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Call(target),
                spokenResponse = "$target کو کال ملائی جا رہی ہے$honorific۔",
                actionLabel = "CALL_$target"
            )
        }

        // 5. WhatsApp (واٹس ایپ)
        if (clean.startsWith("whatsapp") || clean.contains("send whatsapp") ||
            clean.contains("واٹس ایپ") || clean.contains("واٹسایپ")
        ) {
            val msg = clean
                .removePrefix("whatsapp")
                .removePrefix("send whatsapp")
                .replace("واٹس ایپ کھولو", "")
                .replace("واٹس ایپ", "")
                .trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SendWhatsApp(null, if (msg.isNotBlank()) msg else "السلام علیکم"),
                spokenResponse = "واٹس ایپ کھولا جا رہا ہے$honorific۔",
                actionLabel = "WHATSAPP"
            )
        }

        // 6. SMS / Messaging (پیغام)
        if (clean.contains("send message") || clean.contains("sms") || clean.contains("message bhej") ||
            clean.contains("میسج بھیجو") || clean.contains("پیغام بھیجو") || clean.contains("ایس ایم ایس")
        ) {
            val msg = clean
                .replace(Regex("(send message|sms|message bhej|ko|to|میسج بھیجو|پیغام بھیجو|کو)"), "")
                .trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SendSms("", if (msg.isNotBlank()) msg else "السلام علیکم"),
                spokenResponse = "پیغام کی ونڈو کھولی جا رہی ہے$honorific۔",
                actionLabel = "SEND_SMS"
            )
        }

        // 7. Volume & Acoustics (آواز)
        if (clean.contains("volume up") || clean.contains("awaz badhao") || clean.contains("louder") ||
            clean.contains("آواز بڑھاؤ") || clean.contains("آواز تیز کرو") || clean.contains("آواز اونچی کرو")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.AdjustVolume(true),
                spokenResponse = "آواز تیز کر دی گئی ہے$honorific۔",
                actionLabel = "VOLUME_UP"
            )
        }
        if (clean.contains("volume down") || clean.contains("awaz kam karo") || clean.contains("quieter") ||
            clean.contains("آواز کم کرو") || clean.contains("آواز آہستہ کرو") || clean.contains("آواز دھیمی کرو")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.AdjustVolume(false),
                spokenResponse = "آواز کم کر دی گئی ہے$honorific۔",
                actionLabel = "VOLUME_DOWN"
            )
        }
        if (clean.contains("silent") || clean.contains("mute") || clean.contains("shant") ||
            clean.contains("سائلنٹ کرو") || clean.contains("خاموش کرو") || clean.contains("سائلنٹ موڈ")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SetRinger("SILENT"),
                spokenResponse = "فون کو سائلنٹ موڈ پر لگا دیا گیا ہے$honorific۔",
                actionLabel = "RINGER_SILENT"
            )
        }
        if (clean.contains("normal sound") || clean.contains("unmute") || clean.contains("ringer normal") ||
            clean.contains("نارمل موڈ") || clean.contains("گھنٹی کھولو") || clean.contains("آواز کھولو")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.SetRinger("NORMAL"),
                spokenResponse = "فون کی گھنٹی بحال کر دی گئی ہے$honorific۔",
                actionLabel = "RINGER_NORMAL"
            )
        }

        // 8. Battery & Power Status (بیٹری)
        if (clean.contains("battery") || clean.contains("power level") || clean.contains("charge kitna") || clean.contains("energy") ||
            clean.contains("بیٹری بتاؤ") || clean.contains("بیٹری کتنی ہے") || clean.contains("کتنا چارج ہے") || clean.contains("بیٹری چارج")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.CheckBattery,
                spokenResponse = "بیٹری کی صورتحال چیک کی جا رہی ہے$honorific۔",
                actionLabel = "CHECK_BATTERY"
            )
        }

        // 9. Schedule Query (آج کا شیڈول)
        if (clean.contains("what is my schedule") || clean.contains("today's agenda") ||
            clean.contains("aaj kya kaam") || clean.contains("read schedule") || clean.contains("my reminders") ||
            clean.contains("show schedule") || clean == "schedule" ||
            clean.contains("آج کیا کام ہے") || clean.contains("شیڈول بتاؤ") || clean.contains("میرا شیڈول") ||
            clean.contains("شیڈول پڑھو") || clean.contains("آج کی مصروفیات")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.ReadSchedule,
                spokenResponse = "آج کے شیڈول کی تفصیلات حاضر ہیں$honorific۔",
                actionLabel = "READ_SCHEDULE"
            )
        }

        // 10. Add Reminder (یاد دہانی)
        if (clean.contains("remind me") || clean.contains("add reminder") || clean.contains("yaad dilana") || clean.startsWith("schedule ") ||
            clean.contains("یاد دلانا") || clean.contains("شیڈول کرو") || clean.contains("کام درج کرو") || clean.contains("یاد دہانی")
        ) {
            val task = clean
                .replace("remind me to", "")
                .replace("remind me", "")
                .replace("add reminder", "")
                .replace("yaad dilana", "")
                .replace("schedule", "")
                .replace("یاد دلانا", "")
                .replace("شیڈول کرو", "")
                .replace("کام درج کرو", "")
                .trim()
            val finalTask = if (task.isNotBlank()) task else "نیا کام"
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.AddReminder(finalTask, "مقررہ وقت"),
                spokenResponse = "آپ کے شیڈول میں '$finalTask' درج کر لیا گیا ہے$honorific۔",
                actionLabel = "ADD_REMINDER"
            )
        }

        // 11. Morning Briefing / Diagnostics (صبح کا بریفنگ)
        if (clean.contains("morning briefing") || clean.contains("good morning") ||
            clean.contains("subah ka briefing") || clean.contains("status report") || clean.contains("system status") ||
            clean.contains("صبح کا بریفنگ") || clean.contains("صبح بخیر") || clean.contains("سسٹم رپورٹ")
        ) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.MorningBriefing,
                spokenResponse = "صبح بخیر جناب! تمام سسٹمز بہترین حالت میں ہیں۔ بیٹری اور میموری بالکل نارمل ہیں اور میں آپ کے اگلے حکم کا منتظر ہوں۔",
                actionLabel = "MORNING_BRIEFING"
            )
        }

        // 12. Hardware Settings (وائی فائی / بلوٹوتھ)
        if (clean.contains("wifi") || clean.contains("wi-fi") || clean.contains("وائی فائی")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.OpenWifiSettings,
                spokenResponse = "وائی فائی کی سیٹنگز کھولی جا رہی ہیں$honorific۔",
                actionLabel = "SETTINGS_WIFI"
            )
        }
        if (clean.contains("bluetooth") || clean.contains("بلوٹوتھ")) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.OpenBluetoothSettings,
                spokenResponse = "بلوٹوتھ کی سیٹنگز کھولی جا رہی ہیں$honorific۔",
                actionLabel = "SETTINGS_BLUETOOTH"
            )
        }

        // 13. App Launching (ایپ کھولنا)
        val launchRegex = Regex("""(?:open|launch|kholo|chalu karo|کھولو|چلاؤ|اوپن کرو)\s+([a-zA-Z0-9\s\u0600-\u06FF]+)""")
        val launchMatch = launchRegex.find(clean)
        if (launchMatch != null) {
            val appTarget = launchMatch.groupValues[1].trim()
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.LaunchApp(appTarget),
                spokenResponse = "$appTarget کھولی جا رہی ہے$honorific۔",
                actionLabel = "LAUNCH_$appTarget"
            )
        }

        // 14. Conversational Queries (بات چیت)
        if (clean.contains("who are you") || clean.contains("tum kaun ho") || clean.contains("what are you") ||
            clean.contains("کون ہو تم") || clean.contains("تم کون ہو") || clean.contains("اپنا تعارف")
        ) {
            val identity = if (wakeWordOption == WakeWordOption.MAYA) {
                "میں مایا ہوں، آپ کی ذاتی نیورل اسسٹنٹ۔ بتائیے میں آپ کی کیا مدد کر سکتی ہوں؟"
            } else {
                "میں جاروس ہوں، آپ کا ذاتی انٹیلیجنٹ اسسٹنٹ۔ میں آپ کے تمام موبائل احکامات پورا کرنے کے لیے ہمہ وقت تیار ہوں۔"
            }
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation(identity),
                spokenResponse = identity,
                actionLabel = "QUERY_IDENTITY"
            )
        }

        if (clean.contains("hello") || clean.contains("hey jarvis") || clean.contains("namaste") ||
            clean.contains("ہیلو") || clean.contains("سلام") || clean.contains("اسلام علیکم") || clean.contains("السلام علیکم")
        ) {
            val greeting = "وعلیکم السلام$honorific! میں بالکل تیار ہوں۔ بتائیے کیا حکم ہے؟"
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation(greeting),
                spokenResponse = greeting,
                actionLabel = "GREETING"
            )
        }

        if (clean.contains("thank you") || clean.contains("thanks") || clean.contains("shukriya") ||
            clean.contains("شکریہ") || clean.contains("بہت شکریہ") || clean.contains("مہربانی")
        ) {
            val thanks = "حوصلہ افزائی کا بہت شکریہ$honorific! آپ کی خدمت میرا فرض ہے۔"
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.Conversation(thanks),
                spokenResponse = thanks,
                actionLabel = "THANKS"
            )
        }

        // 15. Single word app keywords
        val singleWord = clean.trim()
        val commonApps = listOf(
            "youtube", "whatsapp", "camera", "chrome", "maps", "calculator", "settings", "clock",
            "یوٹیوب", "واٹس ایپ", "کیمرہ", "کیلکولیٹر", "سیٹنگز"
        )
        if (commonApps.contains(singleWord)) {
            return ParsedCommand(
                rawText = input,
                action = JarvisAction.LaunchApp(singleWord),
                spokenResponse = "$singleWord کھولی جا رہی ہے$honorific۔",
                actionLabel = "LAUNCH_$singleWord"
            )
        }

        // Fallback: Urdu polite guidance
        val fallback = "معاف کیجیے گا$honorific، میں یہ سمجھ نہیں پایا۔ آپ مجھ سے ٹارچ جلانے، کال ملانے، ایپ کھولنے، یا شیڈول معلوم کرنے کا کہہ سکتے ہیں۔"
        return ParsedCommand(
            rawText = input,
            action = JarvisAction.Unknown,
            spokenResponse = fallback,
            actionLabel = "UNKNOWN"
        )
    }
}
