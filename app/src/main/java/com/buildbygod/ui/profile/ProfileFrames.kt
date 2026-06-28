package com.buildbygod.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentPink
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.Ink
import java.io.File

/** Decorative ring around the profile photo. The first entry is "no frame". */
data class ProfileFrame(val label: String, val colors: List<Color>)

val profileFrames: List<ProfileFrame> = listOf(
    ProfileFrame("None", emptyList()),
    ProfileFrame("Aurora", listOf(AccentBlue, AccentViolet, AccentBlue)),
    ProfileFrame("Sunset", listOf(AccentAmber, AccentPink, AccentAmber)),
    ProfileFrame("Forest", listOf(AccentGreen, AccentBlue, AccentGreen)),
    ProfileFrame("Gold", listOf(Color(0xFFFFE07A), Color(0xFFE8A33D), Color(0xFFFFE07A))),
    ProfileFrame("Flame", listOf(AccentPink, AccentAmber, AccentViolet, AccentPink)),
)

@Composable
fun FramedAvatar(
    photoPath: String?,
    initial: String,
    frameIndex: Int,
    size: Dp,
    fallbackBrush: Brush,
    modifier: Modifier = Modifier
) {
    val frame = profileFrames.getOrElse(frameIndex) { profileFrames[0] }
    val border = if (frame.colors.size >= 2) 4.dp else 0.dp
    val context = LocalContext.current

    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .then(
                if (border > 0.dp) Modifier.background(Brush.sweepGradient(frame.colors))
                else Modifier
            )
            .padding(border),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(fallbackBrush),
            contentAlignment = Alignment.Center
        ) {
            if (!photoPath.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(File(photoPath)).crossfade(true).build(),
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    initial.ifBlank { "A" }.uppercase(),
                    style = MaterialTheme.typography.displayMedium,
                    color = Ink,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
