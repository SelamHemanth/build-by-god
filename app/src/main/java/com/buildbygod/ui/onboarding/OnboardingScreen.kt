package com.buildbygod.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.buildbygod.data.local.PlanGenerator
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.HeightUnit
import com.buildbygod.domain.model.Sex
import com.buildbygod.domain.model.WeightUnit
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.profile.DobField
import com.buildbygod.ui.profile.ExperiencePicker
import com.buildbygod.ui.profile.GoalChips
import com.buildbygod.ui.profile.HeightPicker
import com.buildbygod.ui.profile.SexChips
import com.buildbygod.ui.profile.WeightPicker
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GradientButton
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary
import java.time.LocalDate

@Composable
fun OnboardingScreen(vm: OnboardingViewModel = hiltViewModel()) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableLongStateOf(0L) }
    var sex by remember { mutableStateOf(Sex.MALE) }
    var heightCm by remember { mutableIntStateOf(0) }
    var heightUnit by remember { mutableStateOf(HeightUnit.CM) }
    var weightKg by remember { mutableFloatStateOf(0f) }
    var weightUnit by remember { mutableStateOf(WeightUnit.KG) }
    var experience by remember { mutableStateOf(ExperienceLevel.BEGINNER) }
    var goals by remember { mutableStateOf(setOf(Goal.BUILD_MUSCLE)) }
    var applyPlan by remember { mutableStateOf(true) }

    val age = if (dob > 0) java.time.Period.between(LocalDate.ofEpochDay(dob), LocalDate.now()).years else 0

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text("Welcome to", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
        Text("Build By God", style = MaterialTheme.typography.displayLarge, color = AccentBlue, fontWeight = FontWeight.Bold)
        Text("powered by hemanth", style = MaterialTheme.typography.labelLarge, color = AccentViolet, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(
            "Tell us a little about you to personalize your plan. No sign-up required.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Section("What should we call you?")
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = Surface2,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Section("Date of birth")
        DobField(dobEpochDay = dob, age = age, onPick = { dob = it })

        Section("Sex")
        SexChips(selected = sex, onSelect = { sex = it })

        Section("Height")
        HeightPicker(heightCm = heightCm, unit = heightUnit, onHeightCm = { heightCm = it }, onUnit = { heightUnit = it })

        Section("Weight")
        WeightPicker(weightKg = weightKg, unit = weightUnit, onWeightKg = { weightKg = it }, onUnit = { weightUnit = it })

        Section("Training experience")
        ExperiencePicker(selected = experience, onSelect = { experience = it })

        Section("Your goals (choose any)")
        GoalChips(selected = goals, onToggle = { g ->
            goals = if (g in goals) goals - g else goals + g
        })

        Section("Your suggested plan")
        val planPreview = remember(goals, experience) { PlanGenerator.build(goals, experience) }
        val trainingCount = planPreview.count { !it.isRest }
        GlassCard(Modifier.fillMaxWidth()) {
            Text(
                "$trainingCount-day split, tailored to your goals & experience",
                style = MaterialTheme.typography.labelLarge,
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(10.dp))
            planPreview.filter { !it.isRest }.forEach { dp ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Text(
                        dp.day.name.take(3).lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = AccentViolet,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(44.dp)
                    )
                    Column {
                        Text(dp.title, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(dp.focus, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Set as my weekly plan", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                    Text("You can fully edit it any time in the Plan tab.", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                }
                Switch(
                    checked = applyPlan,
                    onCheckedChange = { applyPlan = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AccentBlue,
                        checkedTrackColor = AccentViolet
                    )
                )
            }
        }

        Spacer(Modifier.height(36.dp))
        GradientButton(
            text = "Start Training",
            modifier = Modifier.fillMaxWidth(),
            onClick = { vm.finish(name, dob, sex, heightCm, heightUnit, weightKg, weightUnit, experience, goals, applyPlan) }
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(24.dp))
    Text(title, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(8.dp))
}
