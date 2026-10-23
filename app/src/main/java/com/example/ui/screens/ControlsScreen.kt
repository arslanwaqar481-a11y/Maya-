package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controller.RingerState
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
import com.example.ui.theme.StatusRed
import com.example.ui.viewmodel.JarvisViewModel

data class AppShortcutItem(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val query: String
)

@Composable
fun ControlsScreen(
    viewModel: JarvisViewModel,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var callTargetNumber by remember { mutableStateOf("") }
    var smsTargetNumber by remember { mutableStateOf("") }
    var smsMessageText by remember { mutableStateOf("") }

    val appDeck = listOf(
        AppShortcutItem("WhatsApp", Icons.Default.Chat, Color(0xFF25D366), "whatsapp"),
        AppShortcutItem("YouTube", Icons.Default.PlayArrow, Color(0xFFFF0000), "youtube"),
        AppShortcutItem("Camera", Icons.Default.CameraAlt, ArcCyan, "camera"),
        AppShortcutItem("Browser", Icons.Default.Language, Color(0xFF4285F4), "chrome"),
        AppShortcutItem("Maps", Icons.Default.Map, Color(0xFF34A853), "maps"),
        AppShortcutItem("Calculator", Icons.Default.Calculate, JarvisGold, "calculator"),
        AppShortcutItem("Clock/Alarm", Icons.Default.Alarm, ArcCyanLight, "clock"),
        AppShortcutItem("Settings", Icons.Default.Settings, HudTextSecondary, "settings"),
        AppShortcutItem("Files", Icons.Default.Folder, Color(0xFFFFB300), "files")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hardware Control Matrix (Torch, Volume, Ringer)
        item {
            HudGlassCard(
                headerTitle = "Hardware Actuators",
                statusIndicator = "BUS CONNECTED",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Torch / Flashlight Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HudSurface)
                            .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isTorchOn) JarvisGold.copy(alpha = 0.2f) else HudBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                    contentDescription = "Torch State",
                                    tint = if (uiState.isTorchOn) JarvisGold else HudTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AUXILIARY OPTICS (TORCH)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = HudTextPrimary
                                )
                                Text(
                                    text = if (uiState.isTorchOn) "STATUS: ILLUMINATING" else "STATUS: EXTINGUISHED",
                                    fontSize = 10.sp,
                                    color = if (uiState.isTorchOn) JarvisGold else HudTextMuted
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.toggleTorch() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (uiState.isTorchOn) JarvisGold else ArcCyan,
                                contentColor = Color(0xFF060B14)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("toggle_torch_button")
                        ) {
                            Text(
                                text = if (uiState.isTorchOn) "OFF" else "ACTIVATE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Acoustic / Volume Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(HudSurface)
                            .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACOUSTIC OUTPUT GAIN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = HudTextPrimary
                            )
                            Text(
                                text = "STREAM LEVEL CONTROL",
                                fontSize = 10.sp,
                                color = HudTextMuted
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { viewModel.adjustVolume(false) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(HudBackground)
                                    .testTag("volume_down_button")
                            ) {
                                Icon(Icons.Default.VolumeDown, contentDescription = "Volume Down", tint = ArcCyan)
                            }
                            IconButton(
                                onClick = { viewModel.adjustVolume(true) },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(HudBackground)
                                    .testTag("volume_up_button")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Volume Up", tint = ArcCyan)
                            }
                        }
                    }

                    // Ringer Mode Selectors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RingerModeChip(
                            modifier = Modifier.weight(1f),
                            label = "NORMAL",
                            icon = Icons.Default.VolumeUp,
                            isSelected = uiState.ringerState == RingerState.NORMAL,
                            onClick = { viewModel.setRingerMode(RingerState.NORMAL) }
                        )
                        RingerModeChip(
                            modifier = Modifier.weight(1f),
                            label = "VIBRATE",
                            icon = Icons.Default.Vibration,
                            isSelected = uiState.ringerState == RingerState.VIBRATE,
                            onClick = { viewModel.setRingerMode(RingerState.VIBRATE) }
                        )
                        RingerModeChip(
                            modifier = Modifier.weight(1f),
                            label = "SILENT",
                            icon = Icons.Default.NotificationsOff,
                            isSelected = uiState.ringerState == RingerState.SILENT,
                            onClick = { viewModel.setRingerMode(RingerState.SILENT) }
                        )
                    }
                }
            }
        }

        // 2. Telephony & Communications Deck
        item {
            HudGlassCard(
                headerTitle = "Direct Telephony Relay",
                statusIndicator = "GSM / VOLTE",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Call input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = callTargetNumber,
                            onValueChange = { callTargetNumber = it },
                            placeholder = { Text("Enter contact number or dial...", color = HudTextMuted, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("call_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = HudCardBorder,
                                focusedTextColor = HudTextPrimary,
                                unfocusedTextColor = HudTextPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (callTargetNumber.isNotBlank()) {
                                    viewModel.phoneController.makeCall(callTargetNumber)
                                    viewModel.speak("Initiating call to $callTargetNumber, Sir.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StatusGreen, contentColor = Color(0xFF060B14)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("dial_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CALL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // SMS / WhatsApp dispatch
                    OutlinedTextField(
                        value = smsMessageText,
                        onValueChange = { smsMessageText = it },
                        placeholder = { Text("Message text for WhatsApp or SMS...", color = HudTextMuted, fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("message_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ArcCyan,
                            unfocusedBorderColor = HudCardBorder,
                            focusedTextColor = HudTextPrimary,
                            unfocusedTextColor = HudTextPrimary
                        ),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val text = if (smsMessageText.isNotBlank()) smsMessageText else "Hello from JARVIS"
                                viewModel.phoneController.sendWhatsApp(callTargetNumber.ifBlank { null }, text)
                                viewModel.speak("Dispatching message through WhatsApp channel, Sir.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366), contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_whatsapp_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WHATSAPP", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                val text = if (smsMessageText.isNotBlank()) smsMessageText else "Notice from JARVIS."
                                viewModel.phoneController.sendSms(callTargetNumber, text)
                                viewModel.speak("Initiating SMS transmission, Sir.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyanDark, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("send_sms_button")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Rapid App Launcher Deck
        item {
            HudGlassCard(
                headerTitle = "System Application Grid",
                statusIndicator = "RAPID ACCESS",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "1-TAP EXECUTIVE LAUNCH",
                        color = HudTextMuted,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )

                    // 3x3 App Launch Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        appDeck.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowItems.forEach { appItem ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(HudSurface)
                                            .border(1.dp, HudCardBorder, RoundedCornerShape(10.dp))
                                            .clickable {
                                                viewModel.phoneController.launchAppByName(appItem.query)
                                                viewModel.speak("Launching ${appItem.name}, Sir.")
                                            }
                                            .padding(vertical = 10.dp, horizontal = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = appItem.icon,
                                                contentDescription = appItem.name,
                                                tint = appItem.color,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = appItem.name,
                                                color = HudTextPrimary,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. System Settings Shortcuts
        item {
            HudGlassCard(
                headerTitle = "Network & System Configuration",
                statusIndicator = "SETTINGS MATRIX",
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SettingShortcutButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Wifi,
                        title = "WI-FI",
                        onClick = { viewModel.phoneController.openWifiSettings() }
                    )
                    SettingShortcutButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Bluetooth,
                        title = "BLUETOOTH",
                        onClick = { viewModel.phoneController.openBluetoothSettings() }
                    )
                    SettingShortcutButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Tune,
                        title = "WAKE CONFIG",
                        onClick = { onOpenSettings() }
                    )
                    SettingShortcutButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Settings,
                        title = "OS SETTINGS",
                        onClick = { viewModel.phoneController.launchAppByName("settings") }
                    )
                }
            }
        }
    }
}

@Composable
private fun RingerModeChip(
    modifier: Modifier = Modifier,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) ArcCyan.copy(alpha = 0.2f) else HudBackground)
            .border(1.dp, if (isSelected) ArcCyan else HudCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ArcCyan else HudTextMuted,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) ArcCyanLight else HudTextMuted
            )
        }
    }
}

@Composable
private fun SettingShortcutButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(HudSurface)
            .border(1.dp, HudCardBorder, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, color = HudTextPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
