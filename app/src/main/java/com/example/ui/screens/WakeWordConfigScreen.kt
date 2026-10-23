package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDark
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardBorderGlow
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.JarvisViewModel
import com.example.voice.WakeWordConfig
import com.example.voice.WakeWordOption

@Composable
fun WakeWordConfigScreen(
    viewModel: JarvisViewModel,
    onRequestMicrophonePermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.wakeWordConfig

    val sensitivityPercent = (config.sensitivity * 100).toInt()
    val sensitivityLabel = when {
        config.sensitivity < 0.40f -> "LOW SENSITIVITY // STRICT FILTER"
        config.sensitivity <= 0.70f -> "BALANCED AMBIENT // RECOMMENDED"
        config.sensitivity <= 0.88f -> "HIGH SENSITIVITY // FAR-FIELD"
        else -> "ULTRA-SENSITIVE // MAXIMUM GAIN"
    }

    val testPhrases = listOf(
        "Hey Jarvis",
        "Maya",
        "Jarvis torch on",
        "Maya what is my schedule",
        "Hey Maya battery status",
        "Testing background noise"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Primary Visual Indicator Card
        item {
            CurrentWakeWordStatusIndicatorCard(
                config = config,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Top System Header
        item {
            HudGlassCard(
                headerTitle = "Acoustic Wake Engine",
                statusIndicator = "OFFLINE DSP KERNEL",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "WAKE-WORD RECOGNITION MATRIX",
                            color = HudTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ArcCyan.copy(alpha = 0.15f))
                                .border(1.dp, ArcCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTIVE: ${config.selectedWakeWord.displayName.uppercase()}",
                                color = ArcCyanLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "Device-local offline phoneme detector for zero-latency, private voice activation without transmitting raw audio to external servers.",
                        color = HudTextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.5.sp
                    )
                }
            }
        }

        // 2. Persona Selection: Hey Jarvis vs MAYA
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SELECT WAKE-WORD PERSONA",
                    color = HudTextMuted,
                    fontSize = 10.5.sp,
                    letterSpacing = 1.2.sp
                )

                // Hey Jarvis Card
                WakeWordPersonaCard(
                    option = WakeWordOption.HEY_JARVIS,
                    isSelected = config.selectedWakeWord == WakeWordOption.HEY_JARVIS,
                    accentColor = ArcCyan,
                    onSelect = { viewModel.selectWakeWord(WakeWordOption.HEY_JARVIS) },
                    onPreviewVoice = { viewModel.testPersonaVoice(WakeWordOption.HEY_JARVIS) },
                    tag = "select_wake_jarvis"
                )

                // MAYA Card
                WakeWordPersonaCard(
                    option = WakeWordOption.MAYA,
                    isSelected = config.selectedWakeWord == WakeWordOption.MAYA,
                    accentColor = JarvisGold,
                    onSelect = { viewModel.selectWakeWord(WakeWordOption.MAYA) },
                    onPreviewVoice = { viewModel.testPersonaVoice(WakeWordOption.MAYA) },
                    tag = "select_wake_maya"
                )
            }
        }

        // 3. Sensitivity Level Control Slider
        item {
            HudGlassCard(
                headerTitle = "Detection Sensitivity",
                statusIndicator = "$sensitivityPercent% CALIBRATION",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACOUSTIC TRIGGER THRESHOLD",
                                color = HudTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = sensitivityLabel,
                                color = if (config.sensitivity > 0.88f) StatusOrange else if (config.sensitivity < 0.40f) StatusGreen else ArcCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "$sensitivityPercent%",
                            color = ArcCyanLight,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Slider
                    Slider(
                        value = config.sensitivity,
                        onValueChange = { viewModel.setSensitivity(it) },
                        valueRange = 0.10f..1.0f,
                        steps = 18,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyan,
                            activeTrackColor = ArcCyan,
                            inactiveTrackColor = HudCardBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sensitivity_slider")
                    )

                    // Visual Sensitivity Bar Indicator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(HudSurface)
                            .border(1.dp, HudCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SensitivityTierBadge("LOW (20%)", "Noisy rooms", config.sensitivity < 0.40f)
                        SensitivityTierBadge("MED (60%)", "Everyday (Rec)", config.sensitivity in 0.40f..0.75f)
                        SensitivityTierBadge("HIGH (85%)", "Far distance", config.sensitivity > 0.75f)
                    }

                    Text(
                        text = "• Higher sensitivity triggers easily from across the room or when whispering, but may occasionally pick up television speech.\n• Lower sensitivity requires clear enunciation close to the device.",
                        color = HudTextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.5.sp
                    )
                }
            }
        }

        // 4. Offline Signal Processing Engine Toggles
        item {
            HudGlassCard(
                headerTitle = "Signal Processing Filters",
                statusIndicator = "HARDWARE ACCELERATED",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Ambient Noise Suppression
                    ToggleOptionRow(
                        title = "Ambient Noise Suppression (DSP)",
                        description = "Applies real-time acoustic echo and spectral noise filters",
                        checked = config.noiseCancellation,
                        onCheckedChange = { viewModel.setNoiseCancellation(it) },
                        tag = "toggle_noise_cancel"
                    )

                    // Wake Chime Audio Feedback
                    ToggleOptionRow(
                        title = "Acoustic Wake Confirmation Chime",
                        description = "Emits immediate holographic chime upon trigger confirmation",
                        checked = config.wakeChimeEnabled,
                        onCheckedChange = { viewModel.setWakeChime(it) },
                        tag = "toggle_wake_chime"
                    )

                    // Low-Power Sensor Mode
                    ToggleOptionRow(
                        title = "Low-Power Acoustic Polling",
                        description = "Throttles CPU cycles during standby to maximize battery life",
                        checked = config.lowPowerMode,
                        onCheckedChange = { viewModel.setLowPowerMode(it) },
                        tag = "toggle_low_power"
                    )
                }
            }
        }

        // 5. Live Wake-Word Detection Simulator & Verification Deck
        item {
            HudGlassCard(
                headerTitle = "Live Calibration & Verification",
                statusIndicator = "SIMULATOR LAB",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "TEST WAKE PHRASE RECOGNITION",
                        color = HudTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap a phrase chip below or speak into the microphone to calibrate the confidence score against your chosen sensitivity threshold.",
                        color = HudTextSecondary,
                        fontSize = 11.sp
                    )

                    // Test phrase chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(testPhrases) { phrase ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(HudSurface)
                                    .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                                    .clickable { viewModel.simulateWakeWordTest(phrase) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "\"$phrase\"",
                                    color = if (phrase.contains(config.selectedWakeWord.displayName, ignoreCase = true)) ArcCyanLight else HudTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Live mic test trigger
                    Button(
                        onClick = {
                            onRequestMicrophonePermission()
                            viewModel.startVoiceListening()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isListening) StatusRed else ArcCyan,
                            contentColor = Color(0xFF060B14)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("test_mic_listen_button")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isListening) "LISTENING... SAY '${config.selectedWakeWord.displayName.uppercase()}'" else "SPEAK WAKE-WORD TO TEST",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Test Result Display
                    AnimatedVisibility(
                        visible = uiState.lastWakeDetection != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        uiState.lastWakeDetection?.let { res ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HudBackground)
                                    .border(
                                        1.dp,
                                        if (res.isTriggered) StatusGreen else StatusRed,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PHRASE: \"${res.detectedPhrase}\"",
                                        color = HudTextPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (res.isTriggered) "TRIGGERED" else "BELOW THRESHOLD",
                                        color = if (res.isTriggered) StatusGreen else StatusRed,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Phonetic Confidence: ${(res.confidenceScore * 100).toInt()}%",
                                        color = HudTextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Sensitivity Gate: ${(config.sensitivity * 100).toInt()}%",
                                        color = ArcCyan,
                                        fontSize = 11.sp
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { res.confidenceScore.coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp)),
                                    color = if (res.isTriggered) StatusGreen else StatusRed,
                                    trackColor = HudSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WakeWordPersonaCard(
    option: WakeWordOption,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    onPreviewVoice: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) HudSurface else HudSurface.copy(alpha = 0.6f))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else HudCardBorder,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onSelect() }
            .padding(14.dp)
            .testTag(tag)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = if (isSelected) 0.25f else 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (option == WakeWordOption.HEY_JARVIS) Icons.Default.Psychology else Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = option.displayName,
                                color = HudTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accentColor)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "SELECTED",
                                        color = Color(0xFF060B14),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = option.personaTitle.uppercase(),
                            color = accentColor,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onPreviewVoice,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAMPLE", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text = option.description,
                color = HudTextSecondary,
                fontSize = 11.5.sp,
                lineHeight = 16.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                option.defaultKeywords.forEach { kw ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(HudBackground)
                            .border(1.dp, HudCardBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "\"$kw\"",
                            color = HudTextMuted,
                            fontSize = 9.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SensitivityTierBadge(tier: String, description: String, isActive: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = tier,
            color = if (isActive) ArcCyanLight else HudTextMuted,
            fontSize = 9.5.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = description,
            color = if (isActive) ArcCyan else HudTextMuted.copy(alpha = 0.7f),
            fontSize = 8.sp
        )
    }
}

@Composable
private fun ToggleOptionRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(HudSurface)
            .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = HudTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = description,
                color = HudTextSecondary,
                fontSize = 10.sp
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = ArcCyan,
                checkedTrackColor = ArcCyan.copy(alpha = 0.3f),
                uncheckedThumbColor = HudTextMuted,
                uncheckedTrackColor = HudBackground
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}

@Composable
fun CurrentWakeWordStatusIndicatorCard(
    config: WakeWordConfig,
    modifier: Modifier = Modifier
) {
    val accentColor = if (config.selectedWakeWord == WakeWordOption.HEY_JARVIS) ArcCyan else JarvisGold
    val sensitivityPercent = (config.sensitivity * 100).toInt()
    val sensitivityTier = when {
        config.sensitivity < 0.40f -> "LOW SENSITIVITY"
        config.sensitivity <= 0.70f -> "BALANCED (REC)"
        config.sensitivity <= 0.88f -> "HIGH SENSITIVITY"
        else -> "ULTRA GAIN"
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_wake_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = HudSurface.copy(alpha = 0.96f)
        ),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.75f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StatusGreen)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE WAKE-WORD & SENSITIVITY STATUS",
                        color = HudTextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accentColor.copy(alpha = 0.16f))
                        .border(1.dp, accentColor.copy(alpha = 0.45f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.5.dp)
                ) {
                    Text(
                        text = "READY & ARMED",
                        color = accentColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Dual Gauge Columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(HudBackground.copy(alpha = 0.8f))
                    .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column 1: Wake-Word Persona
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "SELECTED WAKE-WORD",
                        color = HudTextMuted,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (config.selectedWakeWord == WakeWordOption.HEY_JARVIS) Icons.Default.Psychology else Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = config.selectedWakeWord.displayName.uppercase(),
                            color = HudTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = config.selectedWakeWord.personaTitle,
                        color = accentColor,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(44.dp)
                        .background(HudCardBorder)
                )

                // Column 2: Offline Sensitivity Level
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp)
                ) {
                    Text(
                        text = "OFFLINE SENSITIVITY",
                        color = HudTextMuted,
                        fontSize = 8.5.sp,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$sensitivityPercent%",
                            color = HudTextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = sensitivityTier,
                                color = accentColor,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { config.sensitivity },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = accentColor,
                        trackColor = HudSurface
                    )
                }
            }

            // Bottom Sub-telemetry strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Noise Filter: ${if (config.noiseCancellation) "ACTIVE" else "OFF"}",
                    color = HudTextSecondary,
                    fontSize = 9.5.sp
                )
                Text(
                    text = "Chime: ${if (config.wakeChimeEnabled) "ON" else "OFF"}",
                    color = HudTextSecondary,
                    fontSize = 9.5.sp
                )
                Text(
                    text = "DataStore: SYNCED",
                    color = ArcCyanLight,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
