package com.buildbygod.ui.exercise

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.buildbygod.domain.model.MuscleGroup
import com.buildbygod.ui.theme.TextSecondary

private enum class BodySide { FRONT, BACK }

/** A highlighted muscle region in normalized (0..1) coordinates of the figure cell. */
private data class Region(
    val side: BodySide,
    val cx: Float,
    val cy: Float,
    val rx: Float,
    val ry: Float,
    val mirror: Boolean = false
)

private val muscleRegions: Map<MuscleGroup, List<Region>> = mapOf(
    MuscleGroup.CHEST to listOf(Region(BodySide.FRONT, 0.40f, 0.255f, 0.11f, 0.06f, true)),
    MuscleGroup.ABS to listOf(Region(BodySide.FRONT, 0.50f, 0.37f, 0.085f, 0.10f)),
    MuscleGroup.OBLIQUES to listOf(Region(BodySide.FRONT, 0.385f, 0.39f, 0.045f, 0.085f, true)),
    MuscleGroup.SHOULDERS to listOf(
        Region(BodySide.FRONT, 0.28f, 0.225f, 0.07f, 0.05f, true),
        Region(BodySide.BACK, 0.28f, 0.225f, 0.06f, 0.045f, true)
    ),
    MuscleGroup.BICEPS to listOf(Region(BodySide.FRONT, 0.19f, 0.33f, 0.05f, 0.07f, true)),
    MuscleGroup.TRICEPS to listOf(Region(BodySide.BACK, 0.19f, 0.33f, 0.05f, 0.07f, true)),
    MuscleGroup.FOREARMS to listOf(Region(BodySide.FRONT, 0.17f, 0.46f, 0.045f, 0.07f, true)),
    MuscleGroup.PALMAR_FASCIA to listOf(Region(BodySide.FRONT, 0.16f, 0.545f, 0.05f, 0.045f, true)),
    MuscleGroup.QUADS to listOf(Region(BodySide.FRONT, 0.41f, 0.66f, 0.06f, 0.12f, true)),
    MuscleGroup.ADDUCTORS to listOf(Region(BodySide.FRONT, 0.46f, 0.62f, 0.03f, 0.09f, true)),
    MuscleGroup.ABDUCTORS to listOf(Region(BodySide.FRONT, 0.35f, 0.60f, 0.03f, 0.06f, true)),
    MuscleGroup.HIP_FLEXORS to listOf(Region(BodySide.FRONT, 0.44f, 0.55f, 0.04f, 0.04f, true)),
    MuscleGroup.IT_BAND to listOf(Region(BodySide.FRONT, 0.345f, 0.70f, 0.02f, 0.12f, true)),
    MuscleGroup.PLANTAR_FASCIA to listOf(Region(BodySide.FRONT, 0.41f, 0.965f, 0.055f, 0.02f, true)),
    MuscleGroup.NECK to listOf(
        Region(BodySide.FRONT, 0.5f, 0.16f, 0.05f, 0.035f),
        Region(BodySide.BACK, 0.5f, 0.16f, 0.05f, 0.035f)
    ),
    MuscleGroup.TRAPS to listOf(Region(BodySide.BACK, 0.5f, 0.205f, 0.16f, 0.06f)),
    MuscleGroup.UPPER_BACK to listOf(Region(BodySide.BACK, 0.5f, 0.30f, 0.18f, 0.08f)),
    MuscleGroup.LATS to listOf(Region(BodySide.BACK, 0.37f, 0.36f, 0.08f, 0.10f, true)),
    MuscleGroup.LOWER_BACK to listOf(Region(BodySide.BACK, 0.5f, 0.46f, 0.10f, 0.06f)),
    MuscleGroup.GLUTES to listOf(Region(BodySide.BACK, 0.40f, 0.56f, 0.08f, 0.06f, true)),
    MuscleGroup.HAMSTRINGS to listOf(Region(BodySide.BACK, 0.41f, 0.70f, 0.06f, 0.11f, true)),
    MuscleGroup.CALVES to listOf(Region(BodySide.BACK, 0.41f, 0.86f, 0.05f, 0.08f, true))
)

@Composable
fun MuscleMap(group: MuscleGroup, accent: Color, modifier: Modifier = Modifier) {
    val bodyColor = TextSecondary.copy(alpha = 0.22f)
    val regions = muscleRegions[group] ?: emptyList()
    val fullBody = group == MuscleGroup.FULL_BODY

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FigureCell("Front", Modifier.weight(1f)) {
            drawFigure(if (fullBody) accent.copy(alpha = 0.45f) else bodyColor)
            if (fullBody) return@FigureCell
            regions.filter { it.side == BodySide.FRONT }.forEach { drawRegion(it, accent) }
        }
        FigureCell("Back", Modifier.weight(1f)) {
            drawFigure(if (fullBody) accent.copy(alpha = 0.45f) else bodyColor)
            if (fullBody) return@FigureCell
            regions.filter { it.side == BodySide.BACK }.forEach { drawRegion(it, accent) }
        }
    }
}

@Composable
private fun FigureCell(caption: String, modifier: Modifier = Modifier, draw: DrawScope.() -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.5f)
                .padding(4.dp)
        ) { draw() }
        Spacer(Modifier.height(4.dp))
        Text(caption, style = MaterialTheme.typography.labelMedium, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

private fun DrawScope.drawRegion(r: Region, accent: Color) {
    fun blob(cx: Float) {
        val center = Offset(cx * size.width, r.cy * size.height)
        val glow = Size(r.rx * size.width * 2.6f, r.ry * size.height * 2.6f)
        val core = Size(r.rx * size.width * 2f, r.ry * size.height * 2f)
        drawOval(accent.copy(alpha = 0.22f), topLeft = Offset(center.x - glow.width / 2, center.y - glow.height / 2), size = glow)
        drawOval(accent.copy(alpha = 0.92f), topLeft = Offset(center.x - core.width / 2, center.y - core.height / 2), size = core)
    }
    blob(r.cx)
    if (r.mirror) blob(1f - r.cx)
}

/** Draws a simple, recognizable humanoid silhouette filling the canvas. */
private fun DrawScope.drawFigure(color: Color) {
    val w = size.width
    val h = size.height

    fun capsule(x0: Float, y0: Float, x1: Float, y1: Float) {
        val left = x0 * w
        val top = y0 * h
        val rw = (x1 - x0) * w
        val rh = (y1 - y0) * h
        val radius = minOf(rw, rh) / 2f
        drawRoundRect(
            color = color,
            topLeft = Offset(left, top),
            size = Size(rw, rh),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius)
        )
    }

    // head + neck
    drawCircle(color, radius = 0.15f * w, center = Offset(0.5f * w, 0.09f * h))
    capsule(0.42f, 0.14f, 0.58f, 0.20f)

    // torso
    val torso = Path().apply {
        moveTo(0.26f * w, 0.21f * h)
        lineTo(0.74f * w, 0.21f * h)
        lineTo(0.62f * w, 0.44f * h)
        lineTo(0.66f * w, 0.55f * h)
        lineTo(0.50f * w, 0.57f * h)
        lineTo(0.34f * w, 0.55f * h)
        lineTo(0.38f * w, 0.44f * h)
        close()
    }
    drawPath(torso, color)

    // arms
    capsule(0.12f, 0.21f, 0.24f, 0.52f)
    capsule(0.76f, 0.21f, 0.88f, 0.52f)
    // hands
    drawCircle(color, radius = 0.07f * w, center = Offset(0.18f * w, 0.55f * h))
    drawCircle(color, radius = 0.07f * w, center = Offset(0.82f * w, 0.55f * h))

    // legs
    capsule(0.34f, 0.55f, 0.48f, 0.95f)
    capsule(0.52f, 0.55f, 0.66f, 0.95f)
    // feet
    drawOval(color, topLeft = Offset(0.33f * w, 0.95f * h), size = Size(0.16f * w, 0.04f * h))
    drawOval(color, topLeft = Offset(0.51f * w, 0.95f * h), size = Size(0.16f * w, 0.04f * h))
}
