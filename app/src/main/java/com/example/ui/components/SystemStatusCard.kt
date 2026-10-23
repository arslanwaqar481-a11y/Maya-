package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.DeviceTelemetry
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed

@Composable
fun SystemTelemetryRow(
    telemetry: DeviceTelemetry,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TelemetryMiniGauge(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.BatteryChargingFull,
            label = "CORE BATTERY",
            value = "${telemetry.batteryPercent}%",
            subtext = if (telemetry.isCharging) "CHARGING" else "DRAIN NOMINAL",
            progress = (telemetry.batteryPercent / 100f).coerceIn(0f, 1f),
            accentColor = if (telemetry.batteryPercent < 20) StatusRed else StatusGreen
        )

        TelemetryMiniGauge(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Memory,
            label = "SYS MEMORY",
            value = "${telemetry.ramUsedMb}M",
            subtext = "OF ${telemetry.ramTotalMb}M",
            progress = if (telemetry.ramTotalMb > 0) (telemetry.ramUsedMb.toFloat() / telemetry.ramTotalMb).coerceIn(0f, 1f) else 0.5f,
            accentColor = ArcCyan
        )

        TelemetryMiniGauge(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Storage,
            label = "FREE STORAGE",
            value = "${telemetry.storageFreeGb}GB",
            subtext = "AVAILABLE",
            progress = (telemetry.storageFreeGb / 64f).coerceIn(0.1f, 1f),
            accentColor = JarvisGold
        )
    }
}

@Composable
private fun TelemetryMiniGauge(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String,
    progress: Float,
    accentColor: Color
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(HudSurface.copy(alpha = 0.85f))
            .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                color = HudTextMuted,
                letterSpacing = 0.5.sp
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontSize = 15.sp,
            color = HudTextPrimary,
            letterSpacing = 0.5.sp
        )

        Text(
            text = subtext,
            fontSize = 8.5.sp,
            color = HudTextSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = accentColor,
            trackColor = HudBackground
        )
    }
}
