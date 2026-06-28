package com.buildbygod.ui.profile

import android.net.Uri
import android.os.Build
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.data.datastore.UserProfile
import com.buildbygod.domain.model.ActivityLevel
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentPink
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.GlassDialog
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.Pill
import com.buildbygod.ui.theme.SectionHeader
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import com.buildbygod.ui.theme.liquidGlass
import java.io.File

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    onOpenSettings: () -> Unit,
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
    var showAddUser by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<UserProfile?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) editorUri = uri }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { ok -> if (ok) pendingCameraUri?.let { editorUri = it } }

    LazyColumn(
        Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Profile", style = MaterialTheme.typography.headlineLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = TextSecondary)
                }
            }
        }

        // ---- Identity card ----
        item {
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
                            Icon(Icons.Filled.CameraAlt, contentDescription = "Change photo", tint = com.buildbygod.ui.theme.Ink, modifier = Modifier.size(16.dp))
                        }
                    }
                    Column(Modifier.padding(start = 16.dp).clickable { editingName = true }) {
                        Text(profile.name.ifBlank { "Athlete" }, style = MaterialTheme.typography.headlineMedium, color = TextPrimary)
                        Text(
                            profile.goals.joinToString(" · ") { it.label },
                            style = MaterialTheme.typography.bodyMedium,
                            color = tokens.accent
                        )
                        Text("Tap to edit name · photo · frame", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }
        }

        // ---- Profiles / multi-user ----
        item { SectionHeader("Profiles") }
        item {
            UsersSection(
                users = state.users,
                activeId = profile.id,
                fallback = tokens.accentGradient,
                onSwitch = { vm.switchUser(it) },
                onAdd = { showAddUser = true },
                onRemove = { pendingDelete = it }
            )
        }

        // ---- Workout stats ----
        item { SectionHeader("Your stats") }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Icons.Filled.CheckCircle, AccentGreen, "${state.stats.totalWorkouts}", "Workouts", Modifier.weight(1f))
                StatTile(Icons.Filled.LocalFireDepartment, AccentAmber, "${state.stats.streak}", "Day streak", Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile(Icons.Filled.Timer, AccentBlue, "${state.stats.totalMinutes}", "Total minutes", Modifier.weight(1f))
                StatTile(Icons.Filled.Bolt, AccentPink, "${state.stats.totalCaloriesBurned}", "Calories burned", Modifier.weight(1f))
            }
        }

        // ---- Body details ----
        item { SectionHeader("Date of birth") }
        item { DobField(profile.dobEpochDay, profile.age, onPick = { vm.setDob(it) }) }

        item { SectionHeader("Height") }
        item {
            HeightPicker(
                heightCm = profile.heightCm,
                unit = profile.heightUnit,
                onHeightCm = { vm.setHeight(it) },
                onUnit = { vm.setHeightUnit(it) }
            )
        }

        item { SectionHeader("Weight") }
        item {
            WeightPicker(
                weightKg = if (profile.weightKg > 0f) profile.weightKg else profile.startWeight,
                unit = profile.weightUnit,
                onWeightKg = { vm.setWeight(it) },
                onUnit = { vm.setWeightUnit(it) }
            )
        }

        item { SectionHeader("Sex") }
        item { SexChips(profile.sex) { vm.setSex(it) } }

        item { SectionHeader("Activity level") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ActivityLevel.entries.forEach { Pill(it.label, profile.activityLevel == it, { vm.setActivity(it) }) }
            }
        }

        item { SectionHeader("Training experience") }
        item { ExperiencePicker(profile.experience) { vm.setExperience(it) } }

        // ---- Nutrition ----
        item { SectionHeader("Daily nutrition target") }
        item {
            val n = state.nutrition
            if (n == null) {
                GlassCard(Modifier.fillMaxWidth()) {
                    Text("Complete your body details", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text(
                        "Add your date of birth, height and weight to get personalized calorie and protein targets.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            } else {
                GlassCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text("Calorie target", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Text("${n.calorieTarget} kcal", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("BMR ${n.bmr}  -  TDEE ${n.tdee} kcal", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MacroPill("Protein", "${n.proteinG} g", AccentPink, Modifier.weight(1f))
                        MacroPill("Carbs", "${n.carbsG} g", AccentBlue, Modifier.weight(1f))
                        MacroPill("Fat", "${n.fatG} g", AccentAmber, Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    MacroPill("Water", "${n.waterMl} ml / day", AccentGreen, Modifier.fillMaxWidth())
                }
            }
        }

        // ---- Goals ----
        item { SectionHeader("Goals") }
        item { GoalChips(profile.goals) { vm.toggleGoal(it) } }

        item { SectionHeader("Default reminder lead time") }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 15, 30, 60).forEach { min ->
                    Pill(if (min == 0) "On time" else "$min min before", profile.defaultReminderLead == min, { vm.setReminderLead(min) })
                }
            }
        }

        item {
            NotificationsCard()
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Build By God  ·  v0.2.0", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text("powered by hemanth", style = MaterialTheme.typography.labelMedium, color = tokens.accent, fontWeight = FontWeight.SemiBold)
            Text("All data stays on your device.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Spacer(Modifier.height(90.dp))
        }
    }

    // ---- dialogs ----
    if (editingName) {
        NameDialog(profile.name, onDismiss = { editingName = false }) {
            vm.setName(it); editingName = false
        }
    }

    if (showPhotoOptions) {
        PhotoOptionsDialog(
            hasPhoto = !profile.photoUri.isNullOrBlank(),
            onDismiss = { showPhotoOptions = false },
            onGallery = {
                showPhotoOptions = false
                galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
            onCamera = {
                showPhotoOptions = false
                val uri = createCameraUri(context)
                pendingCameraUri = uri
                cameraLauncher.launch(uri)
            },
            onFrame = { showPhotoOptions = false; showFramePicker = true },
            onRemove = { showPhotoOptions = false; vm.setPhoto(null) }
        )
    }

    if (showFramePicker) {
        FramePickerDialog(
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

    if (showAddUser) {
        AddUserDialog(onDismiss = { showAddUser = false }) { name ->
            vm.addUser(name); showAddUser = false
        }
    }

    pendingDelete?.let { target ->
        GlassDialog(
            onDismiss = { pendingDelete = null },
            title = "Remove profile",
            confirmButton = {
                TextButton(onClick = { vm.removeUser(target.id); pendingDelete = null }) {
                    Text("Remove", color = AccentPink)
                }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel", color = TextSecondary) } }
        ) {
            Text(
                "Remove ${target.name.ifBlank { "this profile" }}? Their saved details will be deleted from this device.",
                color = TextSecondary
            )
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun UsersSection(
    users: List<UserProfile>,
    activeId: String,
    fallback: androidx.compose.ui.graphics.Brush,
    onSwitch: (String) -> Unit,
    onAdd: () -> Unit,
    onRemove: (UserProfile) -> Unit
) {
    val tokens = LocalFitTokens.current
    GlassCard(Modifier.fillMaxWidth()) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            users.forEach { user ->
                val active = user.id == activeId
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .then(if (active) Modifier.background(tokens.accent.copy(alpha = 0.25f)) else Modifier)
                                .clickable { onSwitch(user.id) }
                                .padding(4.dp)
                        ) {
                            FramedAvatar(
                                photoPath = user.photoUri,
                                initial = user.name.take(1),
                                frameIndex = user.profileFrame,
                                size = 58.dp,
                                fallbackBrush = fallback
                            )
                        }
                        if (users.size > 1) {
                            Box(
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(AccentPink)
                                    .clickable { onRemove(user) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Remove ${user.name}", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                    Text(
                        user.name.ifBlank { "Athlete" },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (active) tokens.accent else TextSecondary,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
            // ---- add new profile ----
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .padding(4.dp)
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(Surface2.copy(alpha = 0.6f))
                        .clickable { onAdd() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add profile", tint = tokens.accent, modifier = Modifier.size(26.dp))
                }
                Text("Add", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun AddUserDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val tokens = LocalFitTokens.current
    GlassDialog(
        onDismiss = onDismiss,
        title = "Add profile",
        confirmButton = {
            TextButton(onClick = { onConfirm(text.trim()) }, enabled = text.isNotBlank()) {
                Text("Create", color = tokens.accent)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) } }
    ) {
        Text("Create a new profile. You'll set up their details next.", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            label = { Text("Name") }
        )
    }
}

@Composable
private fun NotificationsCard() {
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    GlassCard(Modifier.fillMaxWidth(), onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }) {
        Text("Enable workout notifications", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
        Text("Allow Build By God to remind you when it's time to train.", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

private fun createCameraUri(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "images").apply { mkdirs() }
    val file = File(dir, "capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
private fun PhotoOptionsDialog(
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
        OptionRow(Icons.Filled.PhotoLibrary, "Choose from gallery", onGallery)
        OptionRow(Icons.Filled.CameraAlt, "Take a photo", onCamera)
        OptionRow(Icons.Filled.CheckCircle, "Choose frame", onFrame)
        if (hasPhoto) OptionRow(Icons.Filled.Delete, "Remove photo", onRemove)
    }
}

@Composable
private fun OptionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
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
private fun FramePickerDialog(
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
private fun NameDialog(initial: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
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

@Composable
private fun StatTile(icon: ImageVector, tint: Color, value: String, label: String, modifier: Modifier = Modifier) {
    GlassCard(modifier) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
    }
}

@Composable
private fun MacroPill(label: String, value: String, tint: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .liquidGlass(RoundedCornerShape(14.dp), bloom = false)
            .padding(12.dp)
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = tint, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
    }
}
