package com.buildbygod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.Ink

/**
 * Shows a looping bundled demo clip when [clipAsset] is set (an animated GIF/WebP under app assets,
 * e.g. "clips/ch_pushup.gif") rendered via Coil; otherwise renders a glossy placeholder.
 * A small "Full video" pill (when a YouTube link exists) and tapping the placeholder both trigger
 * [onPlayExternal].
 */
@Composable
fun DemoMedia(
    clipAsset: String?,
    accent: Color,
    label: String,
    modifier: Modifier = Modifier,
    onPlayExternal: () -> Unit
) {
    if (clipAsset != null) {
        val context = LocalContext.current
        Box(modifier.clip(RoundedCornerShape(0.dp))) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("file:///android_asset/$clipAsset")
                    .crossfade(true)
                    .build(),
                contentDescription = "$label demo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // subtle top gradient for legibility of the overlay pill
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Transparent, Ink.copy(alpha = 0.55f))
                        )
                    )
            )
            FullVideoPill(
                modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
                onClick = onPlayExternal
            )
        }
    } else {
        Box(
            modifier
                .background(
                    Brush.linearGradient(
                        listOf(accent.copy(alpha = 0.35f), AccentViolet.copy(alpha = 0.25f), Ink)
                    )
                )
                .clickable { onPlayExternal() },
            contentAlignment = Alignment.Center
        ) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.08f),
                    modifier = Modifier.size(120.dp).padding(12.dp)
                )
            }
            Box(
                Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentBlue, AccentViolet))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play demo", tint = Ink, modifier = Modifier.size(34.dp))
            }
            Box(Modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.BottomStart) {
                Text(
                    "Tap to watch demo",
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FullVideoPill(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(Brush.linearGradient(listOf(AccentBlue, AccentViolet)))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
        Text("Full video", color = Ink, fontWeight = FontWeight.Bold)
    }
}
