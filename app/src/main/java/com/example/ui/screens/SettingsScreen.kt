package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun SettingsScreen(
    viewModel: JarvisViewModel,
    onRequestMicrophonePermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val config = uiState.wakeWordConfig

    val sensitivityPercent = (config.sensitivity * 100).toInt()
    val sensitivityTier = when {
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
        "Hey Maya battery status"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Visual Status Indicator Card (persisted in DataStore)
        item {
            SettingsStatusVisualCard(config = config)
        }

        // 2. Wake-Word Persona Switcher Section
        item {
            HudGlassCard(
                headerTitle = "Wake-Word Persona Switcher",
                statusIndicator = "DATASTORE PERSISTED",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose your on-device wake-word. Selecting a persona updates voice synthesis pitch, cadence, and offline phoneme recognition.",
                        color = HudTextSecondary,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )

                    // Persona Option A: Hey Jarvis
                    PersonaOptionCard(
                        option = WakeWordOption.HEY_JARVIS,
                        isSelected = config.selectedWakeWord == WakeWordOption.HEY_JARVIS,
                        accentColor = ArcCyan,
                        onSelect = { viewModel.selectWakeWord(WakeWordOption.HEY_JARVIS) },
                        onPreviewVoice = { viewModel.testPersonaVoice(WakeWordOption.HEY_JARVIS) },
                        testTag = "settings_select_jarvis"
                    )

                    // Persona Option B: MAYA
                    PersonaOptionCard(
                        option = WakeWordOption.MAYA,
                        isSelected = config.selectedWakeWord == WakeWordOption.MAYA,
                        accentColor = JarvisGold,
                        onSelect = { viewModel.selectWakeWord(WakeWordOption.MAYA) },
                        onPreviewVoice = { viewModel.testPersonaVoice(WakeWordOption.MAYA) },
                        testTag = "settings_select_maya"
                    )
                }
            }
        }

        // 3. Offline Sensitivity Configuration Slider
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
                                text = sensitivityTier,
                                color = if (config.sensitivity > 0.88f) StatusOrange else if (config.sensitivity < 0.40f) StatusGreen else ArcCyan,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Text(
                            text = "$sensitivityPercent%",
                            color = ArcCyanLight,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Sensitivity Slider connected to DataStore
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
                            .testTag("settings_sensitivity_slider")
                    )

                    // Quick Calibration Tiers
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(HudSurface)
                            .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SensitivityPresetBadge("LOW (25%)", "Noisy / Vehicle", config.sensitivity < 0.40f) {
                            viewModel.setSensitivity(0.25f)
                        }
                        SensitivityPresetBadge("MED (70%)", "Home / Office (Rec)", config.sensitivity in 0.40f..0.75f) {
                            viewModel.setSensitivity(0.70f)
                        }
                        SensitivityPresetBadge("HIGH (90%)", "Far Distance / Quiet", config.sensitivity > 0.75f) {
                            viewModel.setSensitivity(0.90f)
                        }
                    }

                    Text(
                        text = "Threshold adjustments are saved automatically into Android DataStore and persist across device restarts.",
                        color = HudTextMuted,
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // 4. Offline Signal Processing (DSP) & Engine Toggles
        item {
            HudGlassCard(
                headerTitle = "Signal Processing Filters",
                statusIndicator = "OFFLINE DSP",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsSwitchRow(
                        title = "Ambient Noise Suppression (DSP)",
                        subtitle = "Hardware echo cancellation and acoustic spectral noise gating",
                        checked = config.noiseCancellation,
                        onCheckedChange = { viewModel.setNoiseCancellation(it) },
                        tag = "switch_noise_cancel"
                    )

                    SettingsSwitchRow(
                        title = "Acoustic Wake Confirmation Chime",
                        subtitle = "Emits immediate audio tone when wake-word is verified",
                        checked = config.wakeChimeEnabled,
                        onCheckedChange = { viewModel.setWakeChime(it) },
                        tag = "switch_wake_chime"
                    )

                    SettingsSwitchRow(
                        title = "Low-Power Acoustic Standby",
                        subtitle = "Throttles background CPU wake-locks to extend battery life",
                        checked = config.lowPowerMode,
                        onCheckedChange = { viewModel.setLowPowerMode(it) },
                        tag = "switch_low_power"
                    )
                }
            }
        }

        // 5. Live Calibration Simulator
        item {
            HudGlassCard(
                headerTitle = "Acoustic Test Simulator",
                statusIndicator = "CALIBRATION LAB",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Test how well your phone detects \"${config.selectedWakeWord.displayName}\" at $sensitivityPercent% sensitivity:",
                        color = HudTextSecondary,
                        fontSize = 11.5.sp
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
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

                    Button(
                        onClick = {
                            onRequestMicrophonePermission()
                            viewModel.startVoiceListening()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isListening) StatusRed else ArcCyan,
                            contentColor = Color(0xFF060B14)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("settings_test_mic_btn")
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isListening) "LISTENING... SAY '${config.selectedWakeWord.displayName.uppercase()}'" else "SPEAK TO TEST CALIBRATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Simulator Result
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
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = if (res.isTriggered) "TRIGGER VERIFIED" else "BELOW THRESHOLD",
                                        color = if (res.isTriggered) StatusGreen else StatusRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
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

        // 6. Direct Hardware System Shortcuts
        item {
            HudGlassCard(
                headerTitle = "System Subsystem Shortcuts",
                statusIndicator = "ANDROID OS",
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.phoneController.openVoiceInputSettings() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ArcCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Voice Engine", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }

                    OutlinedButton(
                        onClick = { viewModel.phoneController.launchAppByName("settings") },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HudTextSecondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("All Settings", fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsStatusVisualCard(
    config: WakeWordConfig,
    modifier: Modifier = Modifier
) {
    val accentColor = if (config.selectedWakeWord == WakeWordOption.HEY_JARVIS) ArcCyan else JarvisGold
    val sensitivityPercent = (config.sensitivity * 100).toInt()
    val sensitivityTier = when {
        config.sensitivity < 0.40f -> "LOW FILTER"
        config.sensitivity <= 0.70f -> "BALANCED (REC)"
        config.sensitivity <= 0.88f -> "HIGH SENSITIVITY"
        else -> "ULTRA GAIN"
    }

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_status_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = HudSurface.copy(alpha = 0.96f)
        ),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.8f))
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
                        text = "CURRENT WAKE CONFIGURATION",
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
                        text = "ARMED // READY",
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
                        text = "ACTIVE WAKE-WORD",
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
                    text = "DSP Noise Filter: ${if (config.noiseCancellation) "ON" else "OFF"}",
                    color = HudTextSecondary,
                    fontSize = 9.5.sp
                )
                Text(
                    text = "Chime: ${if (config.wakeChimeEnabled) "ON" else "OFF"}",
                    color = HudTextSecondary,
                    fontSize = 9.5.sp
                )
                Text(
                    text = "DataStore: PERSISTED",
                    color = ArcCyanLight,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PersonaOptionCard(
    option: WakeWordOption,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    onPreviewVoice: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) HudSurface else HudSurface.copy(alpha = 0.6f))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) accentColor else HudCardBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
            .testTag(testTag)
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
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = if (isSelected) 0.25f else 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (option == WakeWordOption.HEY_JARVIS) Icons.Default.Psychology else Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
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
                                        text = "ACTIVE",
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
                            fontSize = 9.5.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                OutlinedButton(
                    onClick = onPreviewVoice,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accentColor),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SAMPLE", fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text = option.description,
                color = HudTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.5.sp
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
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SensitivityPresetBadge(
    tier: String,
    description: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
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
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
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
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = HudTextSecondary,
                fontSize = 9.5.sp
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
