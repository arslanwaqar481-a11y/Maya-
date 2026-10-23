package com.example.controller

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat

data class DeviceTelemetry(
    val batteryPercent: Int = 100,
    val isCharging: Boolean = false,
    val ramUsedMb: Long = 0,
    val ramTotalMb: Long = 0,
    val storageFreeGb: Float = 0f,
    val uptimeHours: Long = 0,
    val uptimeMinutes: Long = 0
)

enum class RingerState {
    NORMAL, VIBRATE, SILENT
}

data class AppLaunchResult(
    val success: Boolean,
    val appName: String,
    val message: String
)

class JarvisPhoneController(private val context: Context) {
    private val TAG = "JarvisPhoneCtrl"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

    private var isTorchOn = false

    fun isTorchEnabled(): Boolean = isTorchOn

    fun toggleTorch(enable: Boolean): Boolean {
        if (cameraManager == null) return false
        return try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager.cameraIdList.firstOrNull()

            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                isTorchOn = enable
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Torch toggle error: ${e.message}")
            false
        }
    }

    fun makeCall(phoneNumber: String, hasCallPermission: Boolean = false): Boolean {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = if (hasCallPermission) {
                Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber"))
            } else {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber"))
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Call failed: ${e.message}")
            false
        }
    }

    fun sendSms(phoneNumber: String, messageText: String): Boolean {
        return try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$cleanNumber")
                putExtra("sms_body", messageText)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Log.e(TAG, "SMS launch failed: ${e.message}")
            false
        }
    }

    fun sendWhatsApp(phoneNumber: String?, messageText: String): Boolean {
        return try {
            if (!phoneNumber.isNullOrBlank()) {
                val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "")
                val url = "https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(messageText)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } else {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    `package` = "com.whatsapp"
                    putExtra(Intent.EXTRA_TEXT, messageText)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            true
        } catch (e: Exception) {
            // Fallback: try opening WhatsApp directly
            launchPackage("com.whatsapp")
        }
    }

    fun launchAppByName(targetName: String): AppLaunchResult {
        val lower = targetName.lowercase().trim()
        return when {
            lower.contains("whatsapp") -> {
                val success = launchPackage("com.whatsapp")
                AppLaunchResult(success, "WhatsApp", if (success) "Opening WhatsApp" else "WhatsApp not installed")
            }
            lower.contains("youtube") -> {
                val success = launchPackage("com.google.android.youtube") || openUrl("https://youtube.com")
                AppLaunchResult(success, "YouTube", "Launching YouTube")
            }
            lower.contains("camera") -> {
                val success = launchIntent(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
                AppLaunchResult(success, "Camera", "Engaging Camera Optics")
            }
            lower.contains("chrome") || lower.contains("browser") -> {
                val success = launchPackage("com.android.chrome") || openUrl("https://google.com")
                AppLaunchResult(success, "Browser", "Opening Browser")
            }
            lower.contains("maps") || lower.contains("navigation") -> {
                val success = launchPackage("com.google.android.apps.maps") || openUrl("geo:0,0")
                AppLaunchResult(success, "Google Maps", "Displaying Navigational Grid")
            }
            lower.contains("calculator") -> {
                val success = launchPackage("com.google.android.calculator") ||
                        launchPackage("com.android.calculator2") ||
                        launchIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR))
                AppLaunchResult(success, "Calculator", "Launching Computational Matrix")
            }
            lower.contains("clock") || lower.contains("alarm") -> {
                val success = launchIntent(Intent(AlarmClock.ACTION_SHOW_ALARMS)) ||
                        launchPackage("com.google.android.deskclock")
                AppLaunchResult(success, "Clock", "Opening Chronometer")
            }
            lower.contains("setting") -> {
                val success = launchIntent(Intent(Settings.ACTION_SETTINGS))
                AppLaunchResult(success, "Settings", "Accessing System Core Configuration")
            }
            lower.contains("files") || lower.contains("file manager") -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "*/*" }
                val success = launchIntent(intent)
                AppLaunchResult(success, "Files", "Opening Local Storage Archive")
            }
            else -> {
                // Dynamic query against installed apps
                val found = findInstalledApp(lower)
                if (found != null) {
                    val intent = context.packageManager.getLaunchIntentForPackage(found)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                        AppLaunchResult(true, lower, "Launching $lower")
                    } else {
                        AppLaunchResult(false, lower, "Unable to launch $lower")
                    }
                } else {
                    AppLaunchResult(false, lower, "App '$lower' not found in database")
                }
            }
        }
    }

    private fun findInstalledApp(query: String): String? {
        return try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(0)
            for (app in packages) {
                val label = pm.getApplicationLabel(app).toString().lowercase()
                if (label.contains(query) || app.packageName.lowercase().contains(query)) {
                    return app.packageName
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    fun launchPackage(packageName: String): Boolean {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun launchIntent(intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun setRingerMode(mode: RingerState): Boolean {
        if (audioManager == null) return false
        return try {
            when (mode) {
                RingerState.SILENT -> audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                RingerState.VIBRATE -> audioManager.ringerMode = AudioManager.RINGER_MODE_VIBRATE
                RingerState.NORMAL -> audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Ringer set failed: ${e.message}")
            false
        }
    }

    fun getRingerMode(): RingerState {
        return when (audioManager?.ringerMode) {
            AudioManager.RINGER_MODE_SILENT -> RingerState.SILENT
            AudioManager.RINGER_MODE_VIBRATE -> RingerState.VIBRATE
            else -> RingerState.NORMAL
        }
    }

    fun adjustVolume(increase: Boolean) {
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        audioManager?.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, AudioManager.FLAG_SHOW_UI)
    }

    fun getVolumeLevel(): Int {
        val current = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        val max = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15
        return if (max > 0) (current * 100) / max else 0
    }

    fun openWifiSettings() = launchIntent(Intent(Settings.ACTION_WIFI_SETTINGS))
    fun openBluetoothSettings() = launchIntent(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    fun openDisplaySettings() = launchIntent(Intent(Settings.ACTION_DISPLAY_SETTINGS))
    fun openVoiceInputSettings() = launchIntent(Intent(Settings.ACTION_VOICE_INPUT_SETTINGS))

    fun getTelemetry(): DeviceTelemetry {
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, batteryFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) (level * 100) / scale else 85
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)
        val totalMb = memInfo.totalMem / (1024 * 1024)
        val availMb = memInfo.availMem / (1024 * 1024)
        val usedMb = totalMb - availMb

        val stat = StatFs(Environment.getDataDirectory().path)
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val freeGb = (freeBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)).toFloat()

        val uptimeMillis = SystemClock.elapsedRealtime()
        val totalSecs = uptimeMillis / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60

        return DeviceTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            ramUsedMb = usedMb,
            ramTotalMb = totalMb,
            storageFreeGb = String.format("%.1f", freeGb).toFloatOrNull() ?: 12f,
            uptimeHours = hours,
            uptimeMinutes = mins
        )
    }
}
