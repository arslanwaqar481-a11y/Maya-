package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.HudBackground
import com.example.ui.theme.HudCardBorder
import com.example.ui.theme.HudCardBorderGlow
import com.example.ui.theme.HudSurface
import com.example.ui.theme.HudTextSecondary
import com.example.ui.theme.JarvisGold

@Composable
fun HolographicWaveform(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float, // 0..10
    modifier: Modifier = Modifier,
    barCount: Int = 24
) {
    val transition = rememberInfiniteTransition(label = "WaveformAnim")

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(38.dp)
    ) {
        val totalWidth = size.width
        val canvasHeight = size.height
        val barWidth = (totalWidth / (barCount * 1.6f)).coerceIn(2.5f, 8f)
        val gap = (totalWidth - (barWidth * barCount)) / (barCount + 1)

        val isActive = isListening || isSpeaking
        val baseMultiplier = if (isActive) (0.3f + (audioLevel / 10f) * 0.7f) else 0.12f

        for (i in 0 until barCount) {
            val sinVal = kotlin.math.sin(phase + (i * 0.35f))
            val normalizedHeight = ((sinVal + 1f) / 2f) * baseMultiplier
            val barH = (canvasHeight * normalizedHeight).coerceAtLeast(4f)

            val x = gap + i * (barWidth + gap)
            val y = (canvasHeight - barH) / 2f

            val barColor = when {
                isListening -> if (i % 2 == 0) ArcCyan else ArcCyanLight
                isSpeaking -> if (i % 2 == 0) JarvisGold else ArcCyan
                else -> ArcCyan.copy(alpha = 0.3f)
            }

            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barH),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}

@Composable
fun HudGlassCard(
    modifier: Modifier = Modifier,
    headerTitle: String? = null,
    statusIndicator: String? = null,
    borderColor: Color = HudCardBorderGlow,
    cornerRadius: Dp = 14.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    listOf(
                        HudSurface.copy(alpha = 0.92f),
                        HudBackground.copy(alpha = 0.96f)
                    )
                )
            )
            .border(1.dp, borderColor, RoundedCornerShape(cornerRadius))
            .padding(14.dp)
    ) {
        if (headerTitle != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = headerTitle.uppercase(),
                    color = ArcCyan,
                    fontSize = 12.sp,
                    letterSpacing = 1.2.sp,
                    style = MaterialTheme.typography.labelSmall
                )
                if (statusIndicator != null) {
                    Text(
                        text = statusIndicator,
                        color = HudTextSecondary,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
        content()
    }
}
