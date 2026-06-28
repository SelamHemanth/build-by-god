package com.buildbygod.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.datastore.UserProfile
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.ProgressRepository
import com.buildbygod.domain.model.ActivityLevel
import com.buildbygod.domain.model.ExperienceLevel
import com.buildbygod.domain.model.Goal
import com.buildbygod.domain.model.HeightUnit
import com.buildbygod.domain.model.NutritionCalculator
import com.buildbygod.domain.model.NutritionPlan
import com.buildbygod.domain.model.Sex
import com.buildbygod.domain.model.WeightUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class WorkoutStats(
    val totalWorkouts: Int = 0,
    val thisWeek: Int = 0,
    val streak: Int = 0,
    val totalMinutes: Long = 0,
    val totalCaloriesBurned: Int = 0
)

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val stats: WorkoutStats = WorkoutStats(),
    val nutrition: NutritionPlan? = null,
    val users: List<UserProfile> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repo: ProfileRepository,
    progressRepo: ProgressRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    val state = combine(repo.profile, progressRepo.sessions(), repo.users) { profile, sessions, users ->
        val weight = if (profile.weightKg > 0f) profile.weightKg else profile.startWeight
        val activeDays = sessions.map { it.epochDay }.toSet()
        val weekStart = today - 6
        val totalCalories = sessions.sumOf {
            NutritionCalculator.caloriesBurned(weight, it.durationSeconds)
        }
        val stats = WorkoutStats(
            totalWorkouts = sessions.size,
            thisWeek = activeDays.count { it in weekStart..today },
            streak = computeStreak(activeDays),
            totalMinutes = sessions.sumOf { it.durationSeconds } / 60,
            totalCaloriesBurned = totalCalories
        )
        val nutrition = if (NutritionCalculator.isComplete(weight, profile.heightCm, profile.age)) {
            NutritionCalculator.compute(profile.sex, weight, profile.heightCm, profile.age, profile.activityLevel, profile.primaryGoal)
        } else null
        ProfileUiState(profile, stats, nutrition, users)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    private fun computeStreak(days: Set<Long>): Int {
        var streak = 0
        var cursor = today
        if (!days.contains(cursor)) cursor -= 1
        while (days.contains(cursor)) { streak += 1; cursor -= 1 }
        return streak
    }

    fun setName(name: String) = update { it.copy(name = name) }
    fun toggleGoal(goal: Goal) = update {
        val next = if (goal in it.goals) it.goals - goal else it.goals + goal
        it.copy(goals = next.ifEmpty { setOf(goal) })
    }
    fun setReminderLead(min: Int) = update { it.copy(defaultReminderLead = min) }
    fun setHeight(cm: Int) = update { it.copy(heightCm = cm) }
    fun setHeightUnit(unit: HeightUnit) = update { it.copy(heightUnit = unit) }
    fun setWeightUnit(unit: WeightUnit) = update { it.copy(weightUnit = unit) }
    fun setWeight(kg: Float) = update {
        it.copy(weightKg = kg, startWeight = if (it.startWeight <= 0f) kg else it.startWeight)
    }
    fun setDob(epochDay: Long) = update { it.copy(dobEpochDay = epochDay) }
    fun setSex(sex: Sex) = update { it.copy(sex = sex) }
    fun setActivity(level: ActivityLevel) = update { it.copy(activityLevel = level) }
    fun setExperience(level: ExperienceLevel) = update { it.copy(experience = level) }
    fun setPhoto(uri: String?) = update { it.copy(photoUri = uri) }
    fun setFrame(index: Int) = update { it.copy(profileFrame = index) }

    fun addUser(name: String) = viewModelScope.launch { repo.addUser(name) }
    fun switchUser(id: String) = viewModelScope.launch { repo.switchUser(id) }
    fun removeUser(id: String) = viewModelScope.launch { repo.removeUser(id) }

    private fun update(transform: (UserProfile) -> UserProfile) {
        viewModelScope.launch { repo.update(transform) }
    }
}
