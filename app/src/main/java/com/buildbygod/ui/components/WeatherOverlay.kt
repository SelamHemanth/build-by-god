package com.buildbygod.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.buildbygod.domain.model.Season
import com.buildbygod.domain.model.WeatherCondition
import com.buildbygod.domain.model.WeatherInfo
import kotlin.math.sin
import kotlin.random.Random

/**
 * A lightweight, full-screen animated weather layer drawn on a single [Canvas]. It renders a soft
 * seasonal tint plus condition-specific particles (rain streaks, drifting snow, fog bands, clouds or
 * calm motes). Designed to sit behind the app content at low opacity so it stays subtle and cheap.
 */
@Composable
fun WeatherOverlay(info: WeatherInfo, modifier: Modifier = Modifier) {
    val time = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) time.floatValue += (now - last) / 1_000_000_000f
                last = now
            }
        }
    }

    // Stable random parameters per particle so motion is smooth frame to frame.
    val particles = remember(info.condition) { buildParticles(info.condition) }
    val tint = seasonTint(info.season)

    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = time.floatValue

        // Seasonal wash from the top.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(tint.copy(alpha = 0.10f), Color.Transparent),
                startY = 0f,
                endY = h * 0.7f
            )
        )

        when (info.condition) {
            WeatherCondition.RAIN, WeatherCondition.STORM -> {
                val color = Color(0xFF9FC2E8)
                particles.forEach { p ->
                    val len = 16f + p.size * 26f
                    val speed = (h * (0.5f + p.speed)) // px/sec
                    val y = (p.offset * h + t * speed) % (h + len) - len
                    val x = p.x * w + sin(t * 0.6f + p.phase) * 6f
                    drawLine(
                        color = color.copy(alpha = 0.18f + p.size * 0.18f),
                        start = Offset(x, y),
                        end = Offset(x - 3f, y + len),
                        strokeWidth = 1.6f,
                        cap = StrokeCap.Round
                    )
                }
                if (info.condition == WeatherCondition.STORM) {
                    // Occasional soft lightning flash.
                    val flash = ((sin(t * 1.3f) + 1f) / 2f)
                    val on = (t.toInt() % 5 == 0) && flash > 0.85f
                    if (on) drawRect(Color.White.copy(alpha = (flash - 0.85f) * 1.2f))
                }
            }

            WeatherCondition.SNOW -> {
                particles.forEach { p ->
                    val speed = h * (0.06f + p.speed * 0.12f)
                    val y = (p.offset * h + t * speed) % (h + 20f) - 20f
                    val x = p.x * w + sin(t * (0.4f + p.speed) + p.phase) * 18f
                    val r = 1.5f + p.size * 3.5f
                    drawCircle(Color.White.copy(alpha = 0.35f + p.size * 0.4f), r, Offset(x, y))
                }
            }

            WeatherCondition.FOG -> {
                for (i in 0 until 3) {
                    val bandY = h * (0.3f + i * 0.2f) + sin(t * 0.2f + i) * 20f
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.06f), Color.Transparent),
                            startY = bandY - 90f,
                            endY = bandY + 90f
                        )
                    )
                }
            }

            WeatherCondition.CLOUDY -> {
                particles.take(5).forEachIndexed { i, p ->
                    val x = ((p.x * w + t * (8f + p.speed * 12f)) % (w + 300f)) - 150f
                    val y = h * (0.12f + i * 0.13f)
                    val r = 80f + p.size * 120f
                    drawCircle(Color.White.copy(alpha = 0.04f), r, Offset(x, y))
                }
            }

            WeatherCondition.CLEAR -> {
                // Soft sun/moon glow + a few slow motes.
                val glow = if (info.isDay) Color(0xFFFFE9A8) else Color(0xFFBFD2FF)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glow.copy(alpha = 0.18f), Color.Transparent),
                        center = Offset(w * 0.82f, h * 0.14f),
                        radius = 260f
                    ),
                    radius = 260f,
                    center = Offset(w * 0.82f, h * 0.14f)
                )
                particles.take(14).forEach { p ->
                    val x = p.x * w + sin(t * 0.3f + p.phase) * 20f
                    val y = (p.offset * h - t * (6f + p.speed * 8f)) % h
                    val yy = if (y < 0) y + h else y
                    drawCircle(glow.copy(alpha = 0.05f + p.size * 0.06f), 1.5f + p.size * 2f, Offset(x, yy))
                }
            }
        }
    }
}

private data class Particle(val x: Float, val offset: Float, val speed: Float, val size: Float, val phase: Float)

private fun buildParticles(condition: WeatherCondition): List<Particle> {
    val count = when (condition) {
        WeatherCondition.RAIN -> 90
        WeatherCondition.STORM -> 120
        WeatherCondition.SNOW -> 70
        WeatherCondition.CLOUDY -> 6
        WeatherCondition.CLEAR -> 16
        WeatherCondition.FOG -> 0
    }
    val rnd = Random(condition.ordinal * 97 + 13)
    return List(count) {
        Particle(
            x = rnd.nextFloat(),
            offset = rnd.nextFloat(),
            speed = rnd.nextFloat(),
            size = rnd.nextFloat(),
            phase = rnd.nextFloat() * 6.28f
        )
    }
}

private fun seasonTint(season: Season): Color = when (season) {
    Season.SPRING -> Color(0xFF66BB6A)
    Season.SUMMER -> Color(0xFFFFC107)
    Season.AUTUMN -> Color(0xFFFF8A50)
    Season.WINTER -> Color(0xFF82B1FF)
}
