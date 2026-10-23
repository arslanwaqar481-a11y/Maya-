package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HudGlassCard
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.HudTextPrimary
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.JarvisGold
import com.example.ui.theme.StatusGreen
import com.example.ui.viewmodel.JarvisViewModel

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.CurrentWakeWordStatusIndicatorCard

@Composable
fun SetupGuideScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(HudBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Wake-Word & Sensitivity Status Indicator Card
        item {
            CurrentWakeWordStatusIndicatorCard(
                config = uiState.wakeWordConfig,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Architecture Summary
        item {
            HudGlassCard(
                headerTitle = "Architecture Blueprint",
                statusIndicator = "100% DEVICE NATIVE",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "JARVIS ANDROID ARCHITECTURE OVERVIEW",
                        color = HudTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Aapka Jarvis Android ke native core subsystems ke upar bana hai. Isme kisi cloud server ya external PC ki zaroorat nahi hai: Offline Speech Recognizer, British Baritone TextToSpeech, CameraManager torch actuator, Telephony/SMS intents, aur Room Offline SQLite Database.",
                        color = HudTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Step 1: Offline Speech-to-Text Setup
        item {
            StepGuideCard(
                stepNumber = "01",
                icon = Icons.Default.Mic,
                title = "Offline Speech-to-Text Setup",
                subtitle = "Bina internet voice recognition ke liye",
                accentColor = ArcCyan,
                instructions = listOf(
                    "Phone Settings > Google > Settings for Google Apps > Search, Assistant & Voice > Voice me jayein.",
                    "'Offline speech recognition' option par tap karein.",
                    "'Installed' tab check karein ya 'All' me jakar English (UK/US) aur Hindi package download karein.",
                    "Ab aapka phone flight mode me bhi aapki voice commands ko pehchan sakega."
                ),
                actionButtonText = "Open Voice Settings",
                onAction = { viewModel.phoneController.openVoiceInputSettings() }
            )
        }

        // Step 2: British TTS Setup
        item {
            StepGuideCard(
                stepNumber = "02",
                icon = Icons.Default.RecordVoiceOver,
                title = "Jarvis Voice Engine (British Male Baritone)",
                subtitle = "Tony Stark ke original Jarvis jaisi voice",
                accentColor = JarvisGold,
                instructions = listOf(
                    "Phone Settings > Accessibility ya General Management > Text-to-speech output me jayein.",
                    "Preferred Engine me 'Speech Recognition and Synthesis by Google' choose karein.",
                    "Gear (⚙️) icon par tap karke 'Install voice data' > English (United Kingdom) me Male voice chunein.",
                    "Pitch humne 0.95f aur speed 1.05f par calibrate ki hai for authentic JARVIS cadence."
                ),
                actionButtonText = "Test Voice Feedback",
                onAction = { viewModel.speak("Greetings, Sir. Voice synthesizers calibrated and online.") }
            )
        }

        // Step 3: Hardware Triggers & MacroDroid Shortcut
        item {
            StepGuideCard(
                stepNumber = "03",
                icon = Icons.Default.Code,
                title = "Hands-Free & Hardware Triggers",
                subtitle = "Bina screen touch kiye Jarvis activate karna",
                accentColor = ArcCyanLight,
                instructions = listOf(
                    "Option A (Default Assistant): Phone Settings > Default Apps > Digital Assistant App me 'JARVIS' select karein. Home button hold ya corner swipe se Jarvis khulega.",
                    "Option B (MacroDroid Integration): MacroDroid app download karein aur 'Volume Button Long Press' ya 'Shake Device' ko Trigger banayein.",
                    "Action me select karein: 'Open App -> JARVIS', taaki jab bhi volume button dabaen, Jarvis screen par aa jaye."
                )
            )
        }

        // Step 4: Battery Optimization Exemption
        item {
            StepGuideCard(
                stepNumber = "04",
                icon = Icons.Default.BatteryAlert,
                title = "Unrestricted Battery Optimization",
                subtitle = "Background service killing prevent karne ke liye",
                accentColor = StatusGreen,
                instructions = listOf(
                    "JARVIS app icon par long press karein > 'App info' (ℹ️) par tap karein.",
                    "'App battery usage' / 'Battery optimization' option me jayein.",
                    "'Unrestricted' / 'Don't optimize' select karein.",
                    "Isse Android OS battery bachaane ke naam par Jarvis ke sensors ko kill nahi karega."
                )
            )
        }

        // Voice Command Cheat Sheet
        item {
            HudGlassCard(
                headerTitle = "Voice Directives Cheat Sheet",
                statusIndicator = "ENGLISH + HINGLISH",
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CommandRow(spoken = "Torch on / Light jalao", action = "Torch illuminates auxiliary flashlight")
                    CommandRow(spoken = "Torch off / Light band karo", action = "Deactivates phone flashlight")
                    CommandRow(spoken = "Call [Name or Number]", action = "Directly initiates dialer / phone call")
                    CommandRow(spoken = "Open [WhatsApp / YouTube / Camera]", action = "Launches requested application")
                    CommandRow(spoken = "Send message to [Person] saying [Text]", action = "Prepares SMS or WhatsApp")
                    CommandRow(spoken = "Battery status / Charge kitna hai", action = "Speaks battery % & power source")
                    CommandRow(spoken = "Morning briefing / Subah ka briefing", action = "Delivers full daily tactical briefing")
                    CommandRow(spoken = "Silent mode / Normal mode", action = "Adjusts ringer audio profile")
                    CommandRow(spoken = "Remind me to [Task] / Yaad dilana", action = "Logs item to Room schedule database")
                    CommandRow(spoken = "What is my schedule / Aaj kya kaam hai", action = "Reads out pending agenda items")
                }
            }
        }
    }
}

@Composable
private fun StepGuideCard(
    stepNumber: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    instructions: List<String>,
    actionButtonText: String? = null,
    onAction: (() -> Unit)? = null
) {
    HudGlassCard(
        headerTitle = "STEP $stepNumber PROTOCOL",
        statusIndicator = "DIRECTIVE",
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(title, color = HudTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = HudTextSecondary, fontSize = 10.5.sp)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                instructions.forEachIndexed { idx, ins ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Text(
                            text = "${idx + 1}. ",
                            color = accentColor,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ins,
                            color = HudTextPrimary,
                            fontSize = 11.5.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            if (actionButtonText != null && onAction != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color(0xFF060B14)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("action_${stepNumber}")
                ) {
                    Text(actionButtonText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CommandRow(spoken: String, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(HudSurface)
            .border(1.dp, HudCardBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "\"$spoken\"",
            color = ArcCyan,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.48f)
        )
        Text(
            text = action,
            color = HudTextSecondary,
            fontSize = 10.5.sp,
            modifier = Modifier.weight(0.52f)
        )
    }
}
