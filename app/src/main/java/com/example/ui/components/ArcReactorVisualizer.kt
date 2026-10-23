package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.ArcCyanLight
import com.example.ui.theme.JarvisGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    isListening: Boolean,
    isSpeaking: Boolean,
    audioLevel: Float, // 0f .. 10f
    modifier: Modifier = Modifier,
    sizeDp: Dp = 220.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactor")

    // Continuous outer rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening || isSpeaking) 5000 else 18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRot"
    )

    // Inner counter-rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isListening || isSpeaking) 3500 else 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRot"
    )

    // Breathing pulse for standby
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val activeScale = if (isListening || isSpeaking) {
        1f + (audioLevel / 10f) * 0.28f
    } else {
        idlePulse
    }

    val coreGlowColor = when {
        isListening -> ArcCyanLight
        isSpeaking -> JarvisGold
        else -> ArcCyan
    }

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension / 2f - 10f

            // 1. Outermost Aura Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(coreGlowColor.copy(alpha = 0.22f * activeScale), Color.Transparent),
                    center = center,
                    radius = maxRadius * 1.15f
                ),
                radius = maxRadius * 1.1f,
                center = center
            )

            // 2. Outer Ring with Tech Dashes
            rotate(outerRotation, pivot = center) {
                drawCircle(
                    color = coreGlowColor.copy(alpha = 0.4f),
                    radius = maxRadius,
                    center = center,
                    style = Stroke(width = 2.5f)
                )

                // 12 Outer Segment Notches
                for (i in 0 until 12) {
                    val angle = (i * 30.0) * (Math.PI / 180.0)
                    val r1 = maxRadius - 12f
                    val r2 = maxRadius
                    val start = Offset(
                        (center.x + r1 * cos(angle)).toFloat(),
                        (center.y + r1 * sin(angle)).toFloat()
                    )
                    val end = Offset(
                        (center.x + r2 * cos(angle)).toFloat(),
                        (center.y + r2 * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = if (i % 3 == 0) JarvisGold else coreGlowColor,
                        start = start,
                        end = end,
                        strokeWidth = if (i % 3 == 0) 3.5f else 1.8f
                    )
                }
            }

            // 3. Middle Counter-Rotating Ring with Arcs
            rotate(innerRotation, pivot = center) {
                val midRadius = maxRadius * 0.76f
                drawCircle(
                    color = ArcCyan.copy(alpha = 0.5f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 1.5f)
                )

                // Draw 4 distinct energy arcs
                val arcCount = 4
                for (i in 0 until arcCount) {
                    val startAngle = i * 90f + 12f
                    drawArc(
                        color = coreGlowColor,
                        startAngle = startAngle,
                        sweepAngle = 66f,
                        useCenter = false,
                        topLeft = Offset(center.x - midRadius, center.y - midRadius),
                        size = androidx.compose.ui.geometry.Size(midRadius * 2, midRadius * 2),
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Inner Ring with Precision Ticks
            val innerRingRadius = maxRadius * 0.54f
            drawCircle(
                color = ArcCyan.copy(alpha = 0.35f),
                radius = innerRingRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // 5. High-Energy Center Core with dynamic audio pulse
            val coreRadius = (maxRadius * 0.36f) * activeScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        coreGlowColor,
                        coreGlowColor.copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.35f
                ),
                radius = coreRadius * 1.3f,
                center = center
            )

            // Core solid center
            drawCircle(
                color = coreGlowColor,
                radius = coreRadius * 0.65f,
                center = center
            )

            // Inner white-hot energy spark
            drawCircle(
                color = Color.White,
                radius = coreRadius * 0.32f,
                center = center
            )

            // Center triangular geometric prism icon (Stark Arc signature)
            val triSize = coreRadius * 0.55f
            val triPath = Path().apply {
                moveTo(center.x, center.y - triSize)
                lineTo(center.x + triSize * 0.866f, center.y + triSize * 0.5f)
                lineTo(center.x - triSize * 0.866f, center.y + triSize * 0.5f)
                close()
            }
            drawPath(
                path = triPath,
                color = Color(0xFF060B14).copy(alpha = 0.75f),
                style = Stroke(width = 2.5f)
            )
        }
    }
}
