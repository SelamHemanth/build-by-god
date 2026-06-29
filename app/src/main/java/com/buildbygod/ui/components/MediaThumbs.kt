package com.buildbygod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buildbygod.ui.theme.Ink

/**
 * Small rounded thumbnail that shows an exercise's real bundled demo clip (animated WebP from
 * assets) so users recognise the move at a glance. Falls back to a tinted dumbbell when a move has
 * no clip shipped. Fully offline — nothing is fetched from the network.
 */
@Composable
fun ExerciseThumb(
    clipAsset: String?,
    accent: Color,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 46.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 14.dp
) {
    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.4f)))),
        contentAlignment = Alignment.Center
    ) {
        if (clipAsset != null) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$clipAsset")
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(shape)
            )
        } else {
            Icon(
                Icons.Filled.FitnessCenter,
                contentDescription = contentDescription,
                tint = Ink,
                modifier = Modifier.size(size * 0.48f)
            )
        }
    }
}

/**
 * Rounded tile showing an emoji that pictures a food, so users recognise items without reading the
 * name. Emojis are rendered by the OS, keeping the app tiny and fully offline.
 */
@Composable
fun FoodIcon(
    emoji: String,
    accent: Color,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 12.dp
) {
    Box(
        modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.5f).sp, style = MaterialTheme.typography.titleMedium)
    }
}
