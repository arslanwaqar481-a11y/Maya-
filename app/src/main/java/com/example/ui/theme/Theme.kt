package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = Color(0xFF001F2A),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBBE9FF),
    secondary = JarvisGold,
    onSecondary = Color(0xFF432C00),
    secondaryContainer = Color(0xFF5F4100),
    onSecondaryContainer = Color(0xFFFFDEA3),
    tertiary = ArcCyanLight,
    onTertiary = Color(0xFF00363F),
    background = HudBackground,
    onBackground = HudTextPrimary,
    surface = HudSurface,
    onSurface = HudTextPrimary,
    surfaceVariant = HudSurfaceVariant,
    onSurfaceVariant = HudTextSecondary,
    outline = HudCardBorder,
    outlineVariant = Color(0xFF162940)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
