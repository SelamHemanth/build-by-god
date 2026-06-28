package com.buildbygod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.Ink

/**
 * Fully-offline demo. When [clipAsset] is set it loops a bundled animated WebP from app assets
 * (e.g. "clips/pushup.webp") via Coil; otherwise it renders a glossy placeholder. No network,
 * no YouTube — every visual ships inside the app.
 */
@Composable
fun DemoMedia(
    clipAsset: String?,
    accent: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    if (clipAsset != null) {
        val context = LocalContext.current
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("file:///android_asset/$clipAsset")
                .crossfade(true)
                .build(),
            contentDescription = "$label demo",
            contentScale = ContentScale.Crop,
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(
            modifier
                .background(
                    Brush.linearGradient(
                        listOf(accent.copy(alpha = 0.35f), AccentViolet.copy(alpha = 0.25f), Ink)
                    )
                ),
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
                Icon(Icons.Filled.FitnessCenter, contentDescription = label, tint = Ink, modifier = Modifier.size(30.dp))
            }
        }
    }
}
