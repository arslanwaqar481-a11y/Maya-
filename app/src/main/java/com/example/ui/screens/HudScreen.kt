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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.HolographicWaveform
import com.example.ui.components.HudGlassCard
import com.example.ui.components.SystemTelemetryRow
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
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
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun HudScreen(
    viewModel: JarvisViewModel,
    onRequestMicrophonePermission: () -> Unit,
    onOpenWakeWordConfig: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val commandLogs by viewModel.commandLogs.collectAsState()
    val config = uiState.wakeWordConfig

    val quickCommands = listOf(
        "صبح کا بریفنگ",
        "ٹارچ آن کرو",
        "ٹارچ بند کرو",
        "بیٹری بتاؤ",
        "واٹس ایپ کھولو",
        "یوٹیوب چلاؤ",
        "سائلنٹ موڈ",
        "آج کیا کام ہے",
        "تم کون ہو"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top HUD System Banner
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(HudSurface.copy(alpha = 0.6f))
                    .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
                    .clickable { onOpenWakeWordConfig() }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CORE ONLINE // WAKE: ${config.selectedWakeWord.displayName.uppercase()} (${(config.sensitivity * 100).toInt()}%)",
                        color = ArcCyanLight,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = if (uiState.isSpeaking) "AUDIO VOCALIZING" else if (uiState.isListening) "ACOUSTIC SENSING" else "STANDBY READY",
                    color = if (uiState.isSpeaking) JarvisGold else if (uiState.isListening) ArcCyan else HudTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Central Arc Reactor & Voice Engine
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.radialGradient(
                            listOf(
                                HudSurface.copy(alpha = 0.95f),
                                HudBackground
                            )
                        )
                    )
                    .border(1.2.dp, HudCardBorderGlow, RoundedCornerShape(20.dp))
                    .padding(vertical = 16.dp, horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Holographic Arc Reactor Visualizer
                    ArcReactorVisualizer(
                        isListening = uiState.isListening,
                        isSpeaking = uiState.isSpeaking,
                        audioLevel = uiState.audioLevel,
                        sizeDp = 210.dp,
                        modifier = Modifier
                            .clickable {
                                if (uiState.isListening) {
                                    viewModel.stopVoiceListening()
                                } else {
                                    onRequestMicrophonePermission()
                                    viewModel.startVoiceListening()
                                }
                            }
                            .testTag("arc_reactor_touch_target")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio Waveform
                    HolographicWaveform(
                        isListening = uiState.isListening,
                        isSpeaking = uiState.isSpeaking,
                        audioLevel = uiState.audioLevel,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Big Voice Trigger Button
                    Button(
                        onClick = {
                            if (uiState.isListening) {
                                viewModel.stopVoiceListening()
                            } else {
                                onRequestMicrophonePermission()
                                viewModel.startVoiceListening()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.isListening) StatusRed.copy(alpha = 0.85f) else ArcCyan.copy(alpha = 0.85f),
                            contentColor = Color(0xFF060B14)
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(46.dp)
                            .testTag("voice_listen_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.isListening) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.isListening) "سن رہا ہوں // روکیں" else "بولنے کے لیے ٹیپ کریں",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }

                    if (uiState.statusMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.statusMessage ?: "",
                            color = StatusRed,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 3. User Speech Transcript & Jarvis Response Banner
        item {
            HudGlassCard(
                headerTitle = "Direct Acoustic Feed",
                statusIndicator = "OFFLINE NATIVE NLP",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (uiState.speechTranscript.isNotBlank()) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "USER // ",
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = uiState.speechTranscript,
                                color = HudTextPrimary,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "JARVIS // ",
                            color = JarvisGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.jarvisResponse,
                            color = HudTextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // 4. Manual Cyber Command Input
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(HudSurface)
                    .border(1.dp, HudCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.directInputText,
                    onValueChange = { viewModel.updateDirectInput(it) },
                    placeholder = {
                        Text(
                            text = "اردو میں حکم درج کریں (مثلاً: ٹارچ آن، کال کرو)...",
                            color = HudTextMuted,
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("command_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = HudTextPrimary,
                        unfocusedTextColor = HudTextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { viewModel.executeDirectInput() })
                )

                IconButton(
                    onClick = { viewModel.executeDirectInput() },
                    modifier = Modifier.testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Execute Command",
                        tint = ArcCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 5. Quick Command Chips
        item {
            Column {
                Text(
                    text = "RAPID DIRECTIVES",
                    color = HudTextMuted,
                    fontSize = 10.sp,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(quickCommands) { cmd ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(HudSurface.copy(alpha = 0.8f))
                                .border(1.dp, HudCardBorder, RoundedCornerShape(16.dp))
                                .clickable { viewModel.executeCommand(cmd) }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = cmd,
                                color = ArcCyanLight,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // 6. Device Telemetry Gauges
        item {
            SystemTelemetryRow(telemetry = uiState.telemetry)
        }

        // 7. Recent Command Log / Console Feed
        item {
            HudGlassCard(
                headerTitle = "Telemetry Event Log",
                statusIndicator = "${commandLogs.size} DIRECTIVES",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (commandLogs.isEmpty()) {
                        Text(
                            text = "No prior directives logged. Acoustic queue primed.",
                            color = HudTextMuted,
                            fontSize = 11.sp
                        )
                    } else {
                        commandLogs.take(4).forEach { log ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = ">",
                                        color = ArcCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text(
                                        text = log.commandText,
                                        color = HudTextPrimary,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = log.actionType,
                                    color = JarvisGold,
                                    fontSize = 9.5.sp,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
