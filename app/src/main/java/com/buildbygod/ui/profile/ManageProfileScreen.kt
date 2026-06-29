package com.buildbygod.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.ActivityLevel
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GlassDialog
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.Pill
import com.buildbygod.ui.theme.SectionHeader
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import java.io.File

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ManageProfileScreen(
    onBack: () -> Unit,
    vm: ProfileViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val profile = state.profile
    val tokens = LocalFitTokens.current
    val context = LocalContext.current

    var editingName by remember { mutableStateOf(false) }
    var showPhotoOptions by remember { mutableStateOf(false) }
    var showFramePicker by remember { mutableStateOf(false) }
    var editorUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) editorUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok) pendingCameraUri?.let { editorUri = it } }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar(title = "Manage profile", onBack = onBack)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ---- Identity (editable) ----
            GlassCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        FramedAvatar(
                            photoPath = profile.photoUri,
                            initial = profile.name.take(1),
                            frameIndex = profile.profileFrame,
                            size = 84.dp,
                            fallbackBrush = tokens.accentGradient,
                            modifier = Modifier.clickable { showPhotoOptions = true }
                        )
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(tokens.accent)
                                .clickable { showPhotoOptions = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Change photo", tint = Ink, modifier = Modifier.size(16.dp))
                        }
                    }
                    Column(Modifier.padding(start = 16.dp).clickable { editingName = true }) {
                        Text(profile.name.ifBlank { "Athlete" }, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                        Text("Tap to edit name · photo · frame", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }

            SectionHeader("Date of birth")
            DobField(profile.dobEpochDay, profile.age, onPick = { vm.setDob(it) })

            SectionHeader("Height")
            HeightPicker(
                heightCm = profile.heightCm,
                unit = profile.heightUnit,
                onHeightCm = { vm.setHeight(it) },
                onUnit = { vm.setHeightUnit(it) }
            )

            SectionHeader("Weight")
            WeightPicker(
                weightKg = if (profile.weightKg > 0f) profile.weightKg else profile.startWeight,
                unit = profile.weightUnit,
                onWeightKg = { vm.setWeight(it) },
                onUnit = { vm.setWeightUnit(it) }
            )

            SectionHeader("Sex")
            SexChips(profile.sex) { vm.setSex(it) }

            SectionHeader("Activity level")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.entries.forEach { Pill(it.label, profile.activityLevel == it, { vm.setActivity(it) }) }
            }

            SectionHeader("Training experience")
            ExperiencePicker(profile.experience) { vm.setExperience(it) }

            SectionHeader("Goals")
            GoalChips(profile.goals) { vm.toggleGoal(it) }

            SectionHeader("Default reminder lead time")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 15, 30, 60).forEach { min ->
                    Pill(if (min == 0) "On time" else "$min min before", profile.defaultReminderLead == min, { vm.setReminderLead(min) })
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    // ---- dialogs ----
    if (editingName) {
        ManageNameDialog(profile.name, onDismiss = { editingName = false }) {
            vm.setName(it); editingName = false
        }
    }

    if (showPhotoOptions) {
        ManagePhotoOptionsDialog(
            hasPhoto = !profile.photoUri.isNullOrBlank(),
            onDismiss = { showPhotoOptions = false },
            onGallery = {
                showPhotoOptions = false
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCamera = {
                showPhotoOptions = false
                val uri = createManageCameraUri(context)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            },
            onFrame = { showPhotoOptions = false; showFramePicker = true },
            onRemove = { showPhotoOptions = false; vm.setPhoto(null) }
        )
    }

    if (showFramePicker) {
        ManageFramePickerDialog(
            current = profile.profileFrame,
            photoPath = profile.photoUri,
            initial = profile.name.take(1),
            fallback = tokens.accentGradient,
            onDismiss = { showFramePicker = false },
            onPick = { vm.setFrame(it); showFramePicker = false }
        )
    }

    editorUri?.let { uri ->
        PhotoEditorDialog(
            sourceUri = uri,
            onCancel = { editorUri = null },
            onSaved = { path -> vm.setPhoto(path); editorUri = null }
        )
    }
}

private fun createManageCameraUri(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun ManagePhotoOptionsDialog(
    hasPhoto: Boolean,
    onDismiss: () -> Unit,
    onGallery: () -> Unit,
    onCamera: () -> Unit,
    onFrame: () -> Unit,
    onRemove: () -> Unit
) {
    GlassDialog(
        onDismiss = onDismiss,
        title = "Profile photo",
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close", color = TextSecondary) } }
    ) {
        ManageOptionRow(Icons.Filled.PhotoLibrary, "Choose from gallery", onGallery)
        ManageOptionRow(Icons.Filled.CameraAlt, "Take a photo", onCamera)
        ManageOptionRow(Icons.Filled.CheckCircle, "Choose frame", onFrame)
        if (hasPhoto) ManageOptionRow(Icons.Filled.Delete, "Remove photo", onRemove)
    }
}

@Composable
private fun ManageOptionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = LocalFitTokens.current.accent, modifier = Modifier.size(22.dp))
        Spacer(Modifier.size(14.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun ManageFramePickerDialog(
    current: Int,
    photoPath: String?,
    initial: String,
    fallback: androidx.compose.ui.graphics.Brush,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    val tokens = LocalFitTokens.current
    GlassDialog(
        onDismiss = onDismiss,
        title = "Choose a frame",
        confirmButton = { TextButton(onClick = onDismiss) { Text("Done", color = tokens.accent) } }
    ) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            profileFrames.forEachIndexed { index, frame ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .clip(CircleShape)
                            .then(if (index == current) Modifier.background(tokens.accent.copy(alpha = 0.25f)) else Modifier)
                            .clickable { onPick(index) }
                            .padding(4.dp)
                    ) {
                        FramedAvatar(
                            photoPath = photoPath,
                            initial = initial,
                            frameIndex = index,
                            size = 56.dp,
                            fallbackBrush = fallback
                        )
                    }
                    Text(frame.label, style = MaterialTheme.typography.labelMedium, color = if (index == current) tokens.accent else TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun ManageNameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    val tokens = LocalFitTokens.current
    GlassDialog(
        onDismiss = onDismiss,
        title = "Your name",
        confirmButton = { TextButton(onClick = { onConfirm(text) }) { Text("Save", color = tokens.accent) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    ) {
        OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true)
    }
}
