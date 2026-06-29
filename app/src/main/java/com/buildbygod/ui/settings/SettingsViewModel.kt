package com.buildbygod.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.datastore.AppSettings
import com.buildbygod.data.datastore.UserProfile
import com.buildbygod.data.datastore.WaterReminderPrefs
import com.buildbygod.data.datastore.WaterReminderStore
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.SettingsRepository
import com.buildbygod.domain.model.NutritionCalculator
import com.buildbygod.notifications.WaterReminderScheduler
import com.buildbygod.ui.theme.AccentScheme
import com.buildbygod.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
    private val waterStore: WaterReminderStore,
    private val profileRepo: ProfileRepository,
    private val waterScheduler: WaterReminderScheduler
) : ViewModel() {

    val settings = repo.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    val waterPrefs = waterStore.prefs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WaterReminderPrefs())

    /** Daily water target (ml) derived from the user's body profile. */
    val waterTargetMl = profileRepo.profile
        .map { waterTarget(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 2500)

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repo.setThemeMode(mode) }
    fun setAccent(scheme: AccentScheme) = viewModelScope.launch { repo.setAccent(scheme) }
    fun setGlass(value: Float) = viewModelScope.launch { repo.setGlassIntensity(value) }

    fun setWaterEnabled(enabled: Boolean) = viewModelScope.launch {
        waterStore.setEnabled(enabled)
        applyWaterSchedule()
    }

    fun setWaterInterval(minutes: Int) = viewModelScope.launch {
        waterStore.setInterval(minutes)
        applyWaterSchedule()
    }

    fun setWaterWindow(startMinutes: Int, endMinutes: Int) = viewModelScope.launch {
        waterStore.setWindow(startMinutes, endMinutes)
        applyWaterSchedule()
    }

    private suspend fun applyWaterSchedule() {
        val prefs = waterStore.prefs.first()
        val target = waterTarget(profileRepo.profile.first())
        waterScheduler.reschedule(
            enabled = prefs.enabled,
            intervalMinutes = prefs.intervalMinutes,
            startMinutes = prefs.startMinutes,
            endMinutes = prefs.endMinutes,
            dailyTargetMl = target
        )
    }

    private fun waterTarget(profile: UserProfile): Int {
        val weight = if (profile.weightKg > 0f) profile.weightKg else profile.startWeight
        return if (NutritionCalculator.isComplete(weight, profile.heightCm, profile.age)) {
            NutritionCalculator.compute(
                profile.sex, weight, profile.heightCm, profile.age,
                profile.activityLevel, profile.primaryGoal
            ).waterMl
        } else 2500
    }
}
