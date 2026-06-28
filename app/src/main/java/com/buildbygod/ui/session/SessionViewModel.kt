package com.buildbygod.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.local.dao.DayExerciseWithInfo
import com.buildbygod.data.local.entity.SessionLogEntity
import com.buildbygod.data.repository.ProgressRepository
import com.buildbygod.data.repository.WorkoutRepository
import com.buildbygod.domain.model.ExerciseType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class SessionUiState(
    val loading: Boolean = true,
    val title: String = "",
    val items: List<DayExerciseWithInfo> = emptyList(),
    val index: Int = 0,
    val completed: Set<Long> = emptySet(),
    val finished: Boolean = false
) {
    val current: DayExerciseWithInfo? get() = items.getOrNull(index)
    val progress: Float get() = if (items.isEmpty()) 0f else completed.size.toFloat() / items.size
    val isLast: Boolean get() = index >= items.size - 1
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val workoutRepo: WorkoutRepository,
    private val progressRepo: ProgressRepository,
    savedState: SavedStateHandle
) : ViewModel() {

    private val day: Int = savedState["day"] ?: 1
    val startedAt = System.currentTimeMillis()

    private val _state = MutableStateFlow(SessionUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val all = workoutRepo.dayExercises(day).first()
            val ordered = all.sortedWith(
                compareBy({ sectionOrder(it.dxSection) }, { it.dxOrder })
            )
            val title = workoutRepo.day(day).first()?.title ?: "Workout"
            _state.update { it.copy(loading = false, items = ordered, title = title) }
        }
    }

    private fun sectionOrder(section: String) = when (section) {
        ExerciseType.WARMUP.name -> 0
        ExerciseType.MAIN.name -> 1
        else -> 2
    }

    fun markDoneAndNext() {
        _state.update { s ->
            val current = s.current ?: return@update s
            val completed = s.completed + current.dxId
            if (s.isLast) {
                s.copy(completed = completed)
            } else {
                s.copy(completed = completed, index = s.index + 1)
            }
        }
    }

    fun skipNext() {
        _state.update { if (it.isLast) it else it.copy(index = it.index + 1) }
    }

    fun goPrevious() {
        _state.update { if (it.index > 0) it.copy(index = it.index - 1) else it }
    }

    fun finish() {
        val s = _state.value
        viewModelScope.launch {
            progressRepo.logSession(
                SessionLogEntity(
                    epochDay = LocalDate.now().toEpochDay(),
                    dayOfWeek = day,
                    title = s.title,
                    completedCount = s.completed.size,
                    totalCount = s.items.size,
                    durationSeconds = (System.currentTimeMillis() - startedAt) / 1000
                )
            )
            _state.update { it.copy(finished = true) }
        }
    }
}
