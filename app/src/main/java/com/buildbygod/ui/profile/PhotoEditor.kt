package com.buildbygod.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.net.Uri
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GradientButton
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Full-screen photo editor: pinch/drag to crop into the circular avatar, plus brightness and
 * saturation. Saves a 512px PNG into the app's private storage and returns its absolute path.
 */
@Composable
fun PhotoEditorDialog(
    sourceUri: Uri,
    onCancel: () -> Unit,
    onSaved: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var brightness by remember { mutableFloatStateOf(0f) }   // -80..80
    var saturation by remember { mutableFloatStateOf(1f) }   // 0..2
    var canvasSidePx by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(sourceUri) {
        val bmp = withContext(Dispatchers.IO) { loadBitmap(context, sourceUri) }
        if (bmp == null) loadFailed = true else bitmap = bmp
    }

    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Ink)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Edit photo", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Pinch to zoom, drag to position", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            Spacer(Modifier.height(16.dp))

            val bmp = bitmap
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { canvasSidePx = it.width.toFloat() }
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (bmp != null) {
                    ComposeCanvas(
                        Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offset += pan
                                }
                            }
                    ) {
                        val side = size.minDimension
                        val m = buildMatrix(bmp.width, bmp.height, side, scale, offset.x, offset.y)
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                            colorFilter = colorFilterFor(brightness, saturation)
                        }
                        drawIntoCanvas { it.nativeCanvas.drawBitmap(bmp, m, paint) }
                    }
                } else {
                    Text(
                        if (loadFailed) "Couldn't load that image." else "Loading...",
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            SliderRow("Brightness", brightness, -80f, 80f) { brightness = it }
            SliderRow("Saturation", saturation, 0f, 2f) { saturation = it }

            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .clickable { onCancel() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) { Text("Cancel", color = TextPrimary, fontWeight = FontWeight.SemiBold) }

                GradientButton(
                    text = "Save",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val b = bitmap ?: return@GradientButton
                        val side = if (canvasSidePx > 0f) canvasSidePx else 1f
                        val path = renderAvatar(context, b, scale, offset, side, brightness, saturation)
                        onSaved(path)
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = TextSecondary)
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = min..max,
            colors = SliderDefaults.colors(
                thumbColor = AccentBlue,
                activeTrackColor = AccentViolet
            )
        )
    }
}

// ---- bitmap helpers ----

private fun buildMatrix(srcW: Int, srcH: Int, side: Float, scale: Float, offX: Float, offY: Float): Matrix {
    val base = maxOf(side / srcW, side / srcH)
    val e = base * scale
    return Matrix().apply {
        postTranslate(-srcW / 2f, -srcH / 2f)
        postScale(e, e)
        postTranslate(side / 2f + offX, side / 2f + offY)
    }
}

private fun colorFilterFor(brightness: Float, saturation: Float): ColorMatrixColorFilter {
    val cm = ColorMatrix().apply { setSaturation(saturation) }
    val b = brightness
    val bm = ColorMatrix(
        floatArrayOf(
            1f, 0f, 0f, 0f, b,
            0f, 1f, 0f, 0f, b,
            0f, 0f, 1f, 0f, b,
            0f, 0f, 0f, 1f, 0f
        )
    )
    cm.postConcat(bm)
    return ColorMatrixColorFilter(cm)
}

private fun renderAvatar(
    context: Context,
    src: Bitmap,
    scale: Float,
    offsetPreview: Offset,
    previewSidePx: Float,
    brightness: Float,
    saturation: Float
): String {
    val out = 512f
    val k = out / previewSidePx
    val bmp = Bitmap.createBitmap(out.toInt(), out.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
        colorFilter = colorFilterFor(brightness, saturation)
    }
    val m = buildMatrix(src.width, src.height, out, scale, offsetPreview.x * k, offsetPreview.y * k)
    canvas.drawBitmap(src, m, paint)

    val file = File(context.filesDir, "avatar_${System.currentTimeMillis()}.png")
    file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    // keep storage tidy: drop older avatars
    context.filesDir.listFiles { _, n -> n.startsWith("avatar_") && n != file.name }?.forEach { it.delete() }
    return file.absolutePath
}

private fun loadBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        val maxDim = 1600
        var sample = 1
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val raw = context.contentResolver.openInputStream(uri)
            ?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null
        val rotation = readRotation(context, uri)
        if (rotation != 0) {
            val mtx = Matrix().apply { postRotate(rotation.toFloat()) }
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, mtx, true)
        } else raw
    } catch (e: Exception) {
        null
    }
}

private fun readRotation(context: Context, uri: Uri): Int {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            val exif = android.media.ExifInterface(stream)
            when (exif.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL)) {
                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90
                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180
                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) {
        0
    }
}
