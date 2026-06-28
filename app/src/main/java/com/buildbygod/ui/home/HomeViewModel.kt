package com.buildbygod.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.data.repository.ProfileRepository
import com.buildbygod.data.repository.ProgressRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.domain.model.ExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val name: String = "",
    val photoUri: String? = null,
    val profileFrame: Int = 0,
    val todayDow: Int = LocalDate.now().dayOfWeek.value,
    val today: WorkoutDayEntity? = null,
    val warmups: Int = 0,
    val mains: Int = 0,
    val stretches: Int = 0,
    val week: List<WorkoutDayEntity> = emptyList(),
    val completedToday: Boolean = false,
    val streak: Int = 0,
    val totalSessions: Int = 0
) {
    val total: Int get() = warmups + mains + stretches
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    profileRepo: ProfileRepository,
    workoutRepo: WorkoutRepository,
    progressRepo: ProgressRepository
) : ViewModel() {

    private val todayDow = LocalDate.now().dayOfWeek.value
    private val todayEpoch = LocalDate.now().toEpochDay()

    private val todayExercises = workoutRepo.dayExercises(todayDow)

    val state = combine(
        profileRepo.profile,
        workoutRepo.days(),
        todayExercises,
        progressRepo.sessionDays()
    ) { profile, days, todayList, sessionDays ->
        val today = days.firstOrNull { it.dayOfWeek == todayDow }
        HomeUiState(
            name = profile.name,
            photoUri = profile.photoUri,
            profileFrame = profile.profileFrame,
            todayDow = todayDow,
            today = today,
            warmups = todayList.count { it.dxSection == ExerciseType.WARMUP.name },
            mains = todayList.count { it.dxSection == ExerciseType.MAIN.name },
            stretches = todayList.count { it.dxSection == ExerciseType.STRETCH.name },
            week = days,
            completedToday = sessionDays.contains(todayEpoch),
            streak = computeStreak(sessionDays.toSet()),
            totalSessions = sessionDays.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    private fun computeStreak(days: Set<Long>): Int {
        var streak = 0
        var cursor = todayEpoch
        // Allow today to be incomplete without breaking the streak.
        if (!days.contains(cursor)) cursor -= 1
        while (days.contains(cursor)) {
            streak += 1
            cursor -= 1
        }
        return streak
    }
}
