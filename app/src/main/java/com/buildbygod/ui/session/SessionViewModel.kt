package com.buildbygod.ui.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buildbygod.data.datastore.ActiveSessionStore
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
    val paused: Boolean = false,
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
    private val activeSessionStore: ActiveSessionStore,
    savedState: SavedStateHandle
) : ViewModel() {

    private val day: Int = savedState["day"] ?: 1
    var startedAt = System.currentTimeMillis()
        private set

    private val _state = MutableStateFlow(SessionUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val all = workoutRepo.dayExercises(day).first()
            val ordered = all.sortedWith(
                compareBy({ sectionOrder(it.dxSection) }, { it.dxOrder })
            )
            val title = workoutRepo.day(day).first()?.title ?: "Workout"

            // Resume an existing in-progress session for this day, otherwise start fresh.
            val existing = activeSessionStore.session.first()
            if (existing.active && existing.day == day) {
                val restoredIds = existing.completedIdSet().filter { id -> ordered.any { it.dxId == id } }.toSet()
                startedAt = System.currentTimeMillis() - existing.elapsedSeconds() * 1000
                _state.update {
                    it.copy(
                        loading = false,
                        items = ordered,
                        title = title,
                        index = existing.index.coerceIn(0, (ordered.size - 1).coerceAtLeast(0)),
                        completed = restoredIds,
                        paused = false
                    )
                }
                activeSessionStore.setPaused(false)
            } else {
                _state.update { it.copy(loading = false, items = ordered, title = title) }
                activeSessionStore.begin(day, title, ordered.size)
            }
        }
    }

    private fun sectionOrder(section: String) = when (section) {
        ExerciseType.WARMUP.name -> 0
        ExerciseType.MAIN.name -> 1
        else -> 2
    }

    private fun persistProgress(s: SessionUiState) {
        viewModelScope.launch { activeSessionStore.updateProgress(s.index, s.completed) }
    }

    fun markDoneAndNext() {
        _state.update { s ->
            val current = s.current ?: return@update s
            val completed = s.completed + current.dxId
            val next = if (s.isLast) s.copy(completed = completed)
            else s.copy(completed = completed, index = s.index + 1)
            persistProgress(next)
            next
        }
    }

    fun skipNext() {
        _state.update {
            val next = if (it.isLast) it else it.copy(index = it.index + 1)
            persistProgress(next)
            next
        }
    }

    fun goPrevious() {
        _state.update {
            val next = if (it.index > 0) it.copy(index = it.index - 1) else it
            persistProgress(next)
            next
        }
    }

    /** Mark the session paused or running; persisted so Home and the notification stay in sync. */
    fun setPaused(paused: Boolean) {
        if (_state.value.finished) return
        _state.update { it.copy(paused = paused) }
        viewModelScope.launch { activeSessionStore.setPaused(paused) }
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
            activeSessionStore.clear()
            _state.update { it.copy(finished = true) }
        }
    }
}
