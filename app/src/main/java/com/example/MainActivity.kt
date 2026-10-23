package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.ControlsScreen
import com.example.ui.screens.HudScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupGuideScreen
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudTextMuted
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JarvisViewModel

enum class JarvisNavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HUD("HUD CORE", Icons.Filled.Mic, Icons.Outlined.Mic, "nav_hud"),
    CONTROLS("CONTROLS", Icons.Filled.Tune, Icons.Outlined.Tune, "nav_controls"),
    SETTINGS("SETTINGS", Icons.Filled.Settings, Icons.Outlined.Settings, "nav_settings"),
    SCHEDULE("SCHEDULE", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_schedule"),
    GUIDE("SETUP GUIDE", Icons.AutoMirrored.Filled.HelpOutline, Icons.AutoMirrored.Outlined.HelpOutline, "nav_guide")
}

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                var currentTab by remember { mutableStateOf(JarvisNavTab.HUD) }

                // Audio permission launcher
                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        viewModel.startVoiceListening()
                    } else {
                        viewModel.speak("Microphone permission was denied, Sir.")
                    }
                }

                // Call permission launcher
                val callPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* handled in call invocation */ }

                val requestAudioPermission: () -> Unit = {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED
                    ) {
                        viewModel.startVoiceListening()
                    } else {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                BackHandler(enabled = currentTab != JarvisNavTab.HUD) {
                    currentTab = JarvisNavTab.HUD
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars),
                    containerColor = HudBackground,
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .height(64.dp)
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                .border(1.dp, HudCardBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                            containerColor = HudSurface,
                            tonalElevation = 8.dp
                        ) {
                            JarvisNavTab.values().forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 8.5.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            letterSpacing = 0.3.sp,
                                            maxLines = 1
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ArcCyan,
                                        selectedTextColor = ArcCyanLight,
                                        indicatorColor = ArcCyan.copy(alpha = 0.15f),
                                        unselectedIconColor = HudTextMuted,
                                        unselectedTextColor = HudTextMuted
                                    ),
                                    modifier = Modifier.testTag(tab.testTag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            JarvisNavTab.HUD -> HudScreen(
                                viewModel = viewModel,
                                onRequestMicrophonePermission = requestAudioPermission,
                                onOpenWakeWordConfig = { currentTab = JarvisNavTab.SETTINGS }
                            )
                            JarvisNavTab.CONTROLS -> ControlsScreen(
                                viewModel = viewModel,
                                onOpenSettings = { currentTab = JarvisNavTab.SETTINGS }
                            )
                            JarvisNavTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel,
                                onRequestMicrophonePermission = requestAudioPermission
                            )
                            JarvisNavTab.SCHEDULE -> ScheduleScreen(
                                viewModel = viewModel
                            )
                            JarvisNavTab.GUIDE -> SetupGuideScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
