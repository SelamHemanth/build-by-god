package com.buildbygod.ui.daydetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.dao.DayExerciseWithInfo
import com.buildbygod.data.local.entity.ExerciseEntity
import com.buildbygod.data.local.entity.WorkoutDayEntity
import com.buildbygod.data.repository.ExerciseRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.domain.model.ExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DayDetailUiState(
    val day: WorkoutDayEntity? = null,
    val warmups: List<DayExerciseWithInfo> = emptyList(),
    val mains: List<DayExerciseWithInfo> = emptyList(),
    val stretches: List<DayExerciseWithInfo> = emptyList()
) {
    val total: Int get() = warmups.size + mains.size + stretches.size
}

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val exerciseRepo: ExerciseRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    val day: Int = savedState["day"] ?: 1

    val state = combine(
        workoutRepo.day(day),
        workoutRepo.dayExercises(day)
    ) { dayEntity, list ->
        DayDetailUiState(
            day = dayEntity,
            warmups = list.filter { it.dxSection == ExerciseType.WARMUP.name },
            mains = list.filter { it.dxSection == ExerciseType.MAIN.name },
            stretches = list.filter { it.dxSection == ExerciseType.STRETCH.name }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayDetailUiState())

    /** Exercises available to add for the given section (by type). */
    fun pickerFor(section: ExerciseType) = exerciseRepo.byType(section.name)

    fun addExercise(ex: ExerciseEntity, section: ExerciseType) {
        viewModelScope.launch {
            workoutRepo.addExerciseToDay(day, ex.id, section.name, ex.defaultSets, ex.defaultReps)
        }
    }

    fun remove(dxId: Long) {
        viewModelScope.launch { workoutRepo.removeDayExercise(dxId) }
    }
}
