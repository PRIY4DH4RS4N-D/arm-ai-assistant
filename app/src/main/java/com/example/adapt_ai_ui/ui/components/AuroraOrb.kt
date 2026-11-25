package com.example.adapt_ai_ui.ui.components

import android.animation.ValueAnimator
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal enum class OrbState(val speed: Double, val glow: Float) {
    IDLE(0.34, 0.75f), LISTENING(0.55, 1f), THINKING(1.45, 1.12f), LOADING(0.8, 0.9f)
}

@Composable
internal fun AuroraOrb(state: OrbState, amplitude: () -> Float, modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var phase by remember { mutableDoubleStateOf(0.0) }
    LaunchedEffect(lifecycle, state) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            if (!ValueAnimator.areAnimatorsEnabled()) return@repeatOnLifecycle
            var previous = withFrameNanos { it }
            while (true) {
                val now = withFrameNanos { it }
                phase += ((now - previous) / 1e9).coerceAtMost(0.06) * state.speed
                previous = now
            }
        }
    }
    Spacer(modifier.drawWithCache {
        val radius = size.minDimension * 0.325f
        val middle = Offset(size.width / 2, size.height / 2)
        val colors = listOf(Color(0xFF1D5BFF), Color(0xFF45DAFF), Color(0xFF7867FF))
        val atmosphere = Brush.radialGradient(
            listOf(Color(0x8E2548ED), Color(0x30226CF9), Color.Transparent), middle, radius * 1.62f
        )
        val sphere = Brush.radialGradient(
            0f to Color(0xFF4CB6FF), 0.23f to Color(0xFF1F56AC),
            0.55f to Color(0xFF132968), 0.82f to Color(0xEF0A1436), 1f to Color(0xC5142E72),
            center = middle - Offset(radius * 0.26f, radius * 0.38f), radius = radius * 1.39f
        )
        val rim = Brush.sweepGradient(
            listOf(Color(0x483B64FF), Color(0xCC7FEAFF), Color(0x406834E5), Color(0x713579FF), Color(0x483B64FF)),
            middle
        )
        val ribbon = Path()
        val edge = Path()
        val ribbons = List(9) { band ->
            val color = colors[band % colors.size]
            Brush.linearGradient(
                0f to Color.Transparent, 0.23f to color.copy(alpha = 0.22f),
                0.50f to color.copy(alpha = 0.76f), 0.69f to Color(0xC0C6F7FF),
                1f to Color.Transparent, start = middle - Offset(radius, radius * 0.52f),
                end = middle + Offset(radius, radius * 0.60f)
            )
        }
        onDrawBehind {
            val t = phase
            val floatY = sin(t * 0.84).toFloat() * radius * 0.047f
            val pulse = 1f + sin(t * 1.5).toFloat() * 0.022f +
                if (state == OrbState.LISTENING) amplitude().coerceIn(0f, 1f) * 0.12f else 0f
            val r = radius * pulse
            translate(top = floatY) {
                drawCircle(atmosphere, radius * 1.64f, middle, alpha = state.glow.coerceAtMost(1f))
                repeat(27) { dot ->
                    val angle = dot * 2.39996 + t * (0.10 + dot % 4 * 0.036)
                    val distance = r * (1.13f + dot % 6 * 0.073f)
                    val position = middle + Offset(cos(angle).toFloat() * distance, sin(angle).toFloat() * distance)
                    val light = (0.18 + (sin(angle * 2 + dot) + 1) * 0.19).toFloat()
                    drawCircle(colors[dot % colors.size], (0.55f + dot % 4 * 0.33f).dp.toPx(), position, light)
                }
                drawCircle(sphere, r, middle)
                repeat(9) { band ->
                    val tilt = band * 0.43 + t * (if (band % 2 == 0) 0.31 else -0.21)
                    val cosine = cos(tilt).toFloat()
                    val sine = sin(tilt).toFloat()
                    fun point(step: Int, side: Float): Offset {
                        val x = (step / 44f * 2 - 1) * 0.99f
                        val envelope = cos(x * PI / 2).toFloat()
                        val y = (sin(x * 4.3 + t * 1.14 + band * 0.73).toFloat() * (0.24f + band * 0.022f) +
                            (band - 4) * 0.067f + side * (0.023f + band % 3 * 0.015f)) * envelope
                        return middle + Offset(x * cosine - y * sine, x * sine + y * cosine) * r
                    }
                    ribbon.reset()
                    edge.reset()
                    for (step in 0..44) {
                        val upper = point(step, 1f)
                        if (step == 0) {
                            ribbon.moveTo(upper.x, upper.y)
                            edge.moveTo(upper.x, upper.y)
                        } else {
                            ribbon.lineTo(upper.x, upper.y)
                            edge.lineTo(upper.x, upper.y)
                        }
                    }
                    for (step in 44 downTo 0) {
                        val lower = point(step, -1f)
                        ribbon.lineTo(lower.x, lower.y)
                    }
                    ribbon.close()
                    drawPath(ribbon, ribbons[band], alpha = (0.65f + band % 3 * 0.12f) * state.glow.coerceAtMost(1f))
                    drawPath(edge, ribbons[band], style = Stroke(width = 1.dp.toPx()))
                }
                drawCircle(rim, r, middle, style = Stroke(1.1.dp.toPx()))
            }
        }
    })
}
